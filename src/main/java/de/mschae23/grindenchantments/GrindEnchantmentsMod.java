/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Grind enchantments.
 *
 * Grind enchantments is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.grindenchantments;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.mschae23.config.api.ConfigIo;
import de.mschae23.config.api.ModConfig;
import de.mschae23.config.api.exception.ConfigException;
import de.mschae23.grindenchantments.config.ClientConfig;
import de.mschae23.grindenchantments.config.ServerConfig;
import de.mschae23.grindenchantments.config.legacy.v1.GrindEnchantmentsConfigV1;
import de.mschae23.grindenchantments.config.legacy.v2.GrindEnchantmentsConfigV2;
import de.mschae23.grindenchantments.config.legacy.v3.GrindEnchantmentsConfigV3;
import de.mschae23.grindenchantments.config.sync.ServerConfigS2CPayload;
import de.mschae23.grindenchantments.cost.CostFunctionType;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import de.mschae23.grindenchantments.impl.DisenchantOperation;
import de.mschae23.grindenchantments.impl.MoveOperation;
import de.mschae23.grindenchantments.impl.ResetRepairCostOperation;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class GrindEnchantmentsMod implements ModInitializer {
    public static final String MODID = "grindenchantments";
    public static final Logger LOGGER = LogManager.getLogger("Grind Enchantments");

    @Nullable
    private static ServerConfig SERVER_CONFIG = null;
    @Nullable
    private static ClientConfig CLIENT_CONFIG = null;

    @Override
    public void onInitialize() {
        GrindEnchantmentsRegistries.init();
        CostFunctionType.init();

        convertLegacyConfig();

        // Singleplayer or on server
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER_CONFIG = initializeServerConfig(server.getRegistryManager());
            SERVER_CONFIG.validateRegistryEntries(server.getRegistryManager());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SERVER_CONFIG = null);

        // Multiplayer
        PayloadTypeRegistry.configurationS2C().register(ServerConfigS2CPayload.ID, ServerConfigS2CPayload.CODEC);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CLIENT_CONFIG = GrindEnchantmentsMod.initializeClientConfig();

            ClientConfigurationNetworking.registerGlobalReceiver(ServerConfigS2CPayload.ID, (payload, context) -> {
                //noinspection resource
                context.client().execute(() -> {
                    log(Level.INFO, "Received server config");
                    // TODO
                });
            });
        });
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, ServerConfigS2CPayload.ID)) {
                log(Level.INFO, "Sent server config");
                ServerConfigurationNetworking.send(handler, new ServerConfigS2CPayload());
            }
        });

        DisenchantOperation disenchant = new DisenchantOperation();
        MoveOperation move = new MoveOperation();
        ResetRepairCostOperation resetRepairCost = new ResetRepairCostOperation();

        GrindstoneEvents.registerAll(disenchant);
        GrindstoneEvents.registerAll(move);
        GrindstoneEvents.registerAll(resetRepairCost);

        ApplyLevelCostEvent.EVENT.register(ApplyLevelCostEvent.DEFAULT, (cost, player) -> {
            player.addExperienceLevels(-cost);
            return true;
        });

        // Mod compatibility with Tax Free Levels
        if (FabricLoader.getInstance().isModLoaded("taxfreelevels")) {
            ApplyLevelCostEvent.EVENT.register(ApplyLevelCostEvent.MOD_COMPATIBILITY, (cost, player) -> {
                TaxFreeLevels.applyFlattenedXpCost(player, cost);
                return true;
            });
        }
    }

    public static ServerConfig getServerConfig() {
        return SERVER_CONFIG == null ? ServerConfig.DEFAULT : SERVER_CONFIG;
    }

    public static ClientConfig getClientConfig() {
        return CLIENT_CONFIG == null ? ClientConfig.DEFAULT : CLIENT_CONFIG;
    }

    public static <C extends ModConfig<C>> ModConfig.Type<C, ? extends ModConfig<C>> getConfigType(ModConfig.Type<C, ? extends ModConfig<C>>[] versions, int version) {
        for (int i = versions.length - 1; i >= 0; i--) {
            ModConfig.Type<C, ? extends ModConfig<C>> v = versions[i];

            if (version == v.version()) {
                return v;
            }
        }

        return versions[versions.length - 1];
    }

    private static <C extends ModConfig<C>> C initializeGenericConfig(Path configName, C latestDefault, Codec<ModConfig<C>> codec,
                                                                      DynamicOps<JsonElement> ops, String kind) {
        // modified version of ConfigIoImpl.initializeConfig from codec-config-api
        Path filePath = FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve(configName);
        C latestConfig = latestDefault;

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            try (InputStream input = Files.newInputStream(filePath)) {
                log(Level.INFO, "Reading " + kind + " config.");

                ModConfig<C> config = ConfigIo.decodeConfig(input, codec, ops);
                latestConfig = config.latest();

                if (config.shouldUpdate() && config.version() < latestDefault.version()) {
                    // Default OpenOptions are CREATE, TRUNCATE_EXISTING, and WRITE
                    try (OutputStream output = Files.newOutputStream(filePath);
                         OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(output))) {
                        log(Level.INFO, "Writing updated " + kind + " config.");

                        ConfigIo.encodeConfig(writer, codec, config.latest(), ops);
                    } catch (IOException e) {
                        log(Level.ERROR, "IO exception while trying to write updated config: " + e.getLocalizedMessage());
                    } catch (ConfigException e) {
                        log(Level.ERROR, e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                log(Level.ERROR, "IO exception while trying to read " + kind + " config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                log(Level.ERROR, e.getLocalizedMessage());
            }
        } else {
            // Write default config if the file doesn't exist
            try (OutputStream output = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                 OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(output))) {
                log(Level.INFO, "Writing default " + kind + " config.");

                ConfigIo.encodeConfig(writer, codec, latestDefault, ops);
            } catch (IOException e) {
                log(Level.ERROR, "IO exception while trying to write config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                log(Level.ERROR, e.getLocalizedMessage());
            }
        }

        return latestConfig;
    }

    public static ClientConfig initializeClientConfig() {
        return initializeGenericConfig(Path.of("client.json"), ClientConfig.DEFAULT, ClientConfig.CODEC,
            JsonOps.INSTANCE, "client");
    }

    private static ServerConfig initializeServerConfig(RegistryWrapper.WrapperLookup wrapperLookup) {
        return initializeGenericConfig(Path.of("server.json"), ServerConfig.DEFAULT, ServerConfig.CODEC,
            RegistryOps.of(JsonOps.INSTANCE, wrapperLookup), "server");
    }

    @SuppressWarnings("deprecation")
    private static void convertLegacyConfig() {
        final Path newConfigDirPath = FabricLoader.getInstance().getConfigDir().resolve(MODID);

        if (!Files.isDirectory(newConfigDirPath)) {
            RegistryWrapper.WrapperLookup wrapperLookup = RegistryWrapper.WrapperLookup.of(Stream.of(GrindEnchantmentsRegistries.COST_FUNCTION));
            Optional<GrindEnchantmentsConfigV3> legacyConfigOpt = readLegacyConfig(wrapperLookup);

            if (legacyConfigOpt.isPresent()) {
                GrindEnchantmentsConfigV3 legacyConfig = legacyConfigOpt.get();
                ServerConfig serverConfig = legacyConfig.toServerConfig();
                ClientConfig clientConfig = legacyConfig.toClientConfig();

                try {
                    final Path serverConfigPath = newConfigDirPath.resolve("server.json");
                    final Path clientConfigPath = newConfigDirPath.resolve("client.json");

                    Files.createDirectories(newConfigDirPath);
                    boolean success = true;

                    try (OutputStream outputStream = Files.newOutputStream(serverConfigPath);
                         OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(outputStream))) {
                        log(Level.INFO, "Writing converted server config.");

                        ConfigIo.encodeConfig(writer, ServerConfig.CODEC, serverConfig, RegistryOps.of(JsonOps.INSTANCE, wrapperLookup));
                    } catch (ConfigException e) {
                        log(Level.ERROR, e.getLocalizedMessage());
                        success = false;
                    }

                    try (OutputStream outputStream = Files.newOutputStream(clientConfigPath);
                         OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(outputStream))) {
                        log(Level.INFO, "Writing converted client config.");

                        ConfigIo.encodeConfig(writer, ClientConfig.CODEC, clientConfig, RegistryOps.of(JsonOps.INSTANCE, wrapperLookup));
                    } catch (ConfigException e) {
                        log(Level.ERROR, e.getLocalizedMessage());
                        success = false;
                    }

                    if (success) {
                        Path configDir = FabricLoader.getInstance().getConfigDir();
                        // Move to grindenchantments.json.old if conversion succeeds
                        Files.move(configDir.resolve(MODID + ".json"), configDir.resolve(MODID + ".json.old"));
                    }
                } catch (IOException e) {
                    log(Level.ERROR, "IO exception while trying to convert legacy config: " + e.getLocalizedMessage());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static Optional<GrindEnchantmentsConfigV3> readLegacyConfig(RegistryWrapper.WrapperLookup wrapperLookup) {
        final GrindEnchantmentsConfigV3 legacyLatestConfigDefault = GrindEnchantmentsConfigV3.DEFAULT;
        final int legacyLatestConfigVersion = legacyLatestConfigDefault.version();
        @SuppressWarnings({"unchecked", "deprecation"})
        final ModConfig.Type<GrindEnchantmentsConfigV3, ? extends ModConfig<GrindEnchantmentsConfigV3>>[] legacyConfigCodecs = new ModConfig.Type[] {
            GrindEnchantmentsConfigV1.TYPE, GrindEnchantmentsConfigV2.TYPE, GrindEnchantmentsConfigV3.TYPE
        };

        final Codec<ModConfig<GrindEnchantmentsConfigV3>> legacyConfigCodec = ModConfig.createCodec(legacyLatestConfigVersion, version ->
            getConfigType(legacyConfigCodecs, version));

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(Paths.get(MODID + ".json"));
        @Nullable
        GrindEnchantmentsConfigV3 config = null;

        if (Files.exists(configPath) && Files.isRegularFile(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                log(Level.INFO, "Reading legacy config.");

                ModConfig<GrindEnchantmentsConfigV3> readConfig = ConfigIo.decodeConfig(input, legacyConfigCodec, RegistryOps.of(JsonOps.INSTANCE, wrapperLookup));
                config = readConfig.latest();
            } catch (IOException e) {
                log(Level.ERROR, "IO exception while trying to read config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                log(Level.ERROR, e.getLocalizedMessage());
            }
        }

        return Optional.ofNullable(config);
    }

    public static void log(Level level, Object message) {
        LOGGER.log(level, "[Grind Enchantments] {}", message);
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}

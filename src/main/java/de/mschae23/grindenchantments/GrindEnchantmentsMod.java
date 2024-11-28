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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
import de.mschae23.grindenchantments.cost.CostFunctionType;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import de.mschae23.grindenchantments.impl.DisenchantOperation;
import de.mschae23.grindenchantments.impl.MoveOperation;
import de.mschae23.grindenchantments.impl.ResetRepairCostOperation;
import de.mschae23.grindenchantments.network.s2c.ServerConfigS2CPayload;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class GrindEnchantmentsMod implements ModInitializer {
    public static final String MODID = "grindenchantments";
    public static final Logger LOGGER = LogManager.getLogger("Grind Enchantments");

    private static GrindEnchantmentsConfigV3 LEGACY_CONFIG = GrindEnchantmentsConfigV3.DEFAULT;
    @Nullable
    private static ServerConfig SERVER_CONFIG = null;
    @Nullable
    private static ClientConfig CLIENT_CONFIG = null;

    @Override
    public void onInitialize() {
        // Singleplayer
        ServerLifecycleEvents.SERVER_STARTING.register(server -> readServerConfig(server.getRegistryManager())
            .ifPresent(config -> SERVER_CONFIG = config));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SERVER_CONFIG = null);

        // Multiplayer
        PayloadTypeRegistry.configurationS2C().register(ServerConfigS2CPayload.ID, ServerConfigS2CPayload.CODEC);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CLIENT_CONFIG = GrindEnchantmentsMod.readClientConfig().orElse(ClientConfig.DEFAULT);

            ClientConfigurationNetworking.registerGlobalReceiver(ServerConfigS2CPayload.ID, (payload, context) -> {
                //noinspection resource
                context.client().execute(() -> {
                    // TODO
                });
            });
        });

        LEGACY_CONFIG = readLegacyConfig().orElse(GrindEnchantmentsConfigV3.DEFAULT);

        GrindEnchantmentsRegistries.init();
        CostFunctionType.init();

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

    @Deprecated
    public static GrindEnchantmentsConfigV3 getLegacyConfig() {
        return LEGACY_CONFIG;
    }

    private static <C extends ModConfig<C>> ModConfig.Type<C, ? extends ModConfig<C>> getConfigType(ModConfig.Type<C, ? extends ModConfig<C>>[] versions, int version) {
        for (int i = versions.length - 1; i >= 0; i--) {
            ModConfig.Type<C, ? extends ModConfig<C>> v = versions[i];

            if (version == v.version()) {
                return v;
            }
        }

        return versions[versions.length - 1];
    }

    private static <C extends ModConfig<C>> Optional<C> readGenericConfig(Path configName, Codec<ModConfig<C>> codec,
                                                                          DynamicOps<JsonElement> ops, String kind) {
        Path filePath = FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve(configName);
        @Nullable
        C config = null;

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            try (InputStream input = Files.newInputStream(filePath)) {
                log(Level.INFO, "Reading " + kind + " config.");

                ModConfig<C> readConfig = ConfigIo.decodeConfig(input, codec, ops);
                config = readConfig.latest();
            } catch (IOException e) {
                log(Level.ERROR, "IO exception while trying to read " + kind + " config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                log(Level.ERROR, e.getLocalizedMessage());
            }
        }

        return Optional.ofNullable(config);
    }

    public static Optional<ClientConfig> readClientConfig() {
        return readGenericConfig(Path.of("client.json"), ModConfig.<ClientConfig>createCodec(ClientConfig.TYPE.version(), version ->
            getConfigType(ClientConfig.VERSIONS, version)), JsonOps.INSTANCE, "client");
    }

    private static Optional<ServerConfig> readServerConfig(RegistryWrapper.WrapperLookup wrapperLookup) {
        return readGenericConfig(Path.of("server.json"), ModConfig.<ServerConfig>createCodec(ServerConfig.TYPE.version(), version ->
            getConfigType(ServerConfig.VERSIONS, version)), RegistryOps.of(JsonOps.INSTANCE, wrapperLookup), "server");
    }

    @SuppressWarnings("deprecation")
    private static Optional<GrindEnchantmentsConfigV3> readLegacyConfig() {
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

                ModConfig<GrindEnchantmentsConfigV3> readConfig = ConfigIo.decodeConfig(input, legacyConfigCodec, JsonOps.INSTANCE);
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

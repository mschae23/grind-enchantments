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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.mschae23.config.api.ConfigIo;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.cost.CostFunctionType;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import de.mschae23.grindenchantments.impl.DisenchantOperation;
import de.mschae23.grindenchantments.impl.MoveOperation;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GrindEnchantmentsMod implements ModInitializer {
    public static final String MODID = "grindenchantments";
    public static final Logger LOGGER = LogManager.getLogger("Grind Enchantments");

    public static final Path CONFIG_PATH = Paths.get(MODID + ".json");

    private static final GrindEnchantmentsV2Config LATEST_CONFIG_DEFAULT = GrindEnchantmentsV2Config.DEFAULT;
    private static final int LATEST_CONFIG_VERSION = LATEST_CONFIG_DEFAULT.version();
    private static final Codec<ModConfig<GrindEnchantmentsV2Config>> CONFIG_CODEC = ModConfig.createCodec(LATEST_CONFIG_VERSION,
        version -> getConfigType(GrindEnchantmentsV2Config.VERSIONS, version));

    private static GrindEnchantmentsV2Config CONFIG = LATEST_CONFIG_DEFAULT;

    @Override
    public void onInitialize() {
        GrindEnchantmentsRegistries.init();
        CostFunctionType.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
            CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
                RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), LOGGER::info, LOGGER::error)
        );

        DisenchantOperation disenchant = new DisenchantOperation();
        MoveOperation move = new MoveOperation();

        GrindstoneEvents.registerAll(disenchant);
        GrindstoneEvents.registerAll(move);

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

    public static GrindEnchantmentsV2Config getConfig() {
        return CONFIG;
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

    public static void initializeConfigOnClient() {
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryWrapper.WrapperLookup.of(Stream.of(
            GrindEnchantmentsRegistries.COST_FUNCTION.getReadOnlyWrapper()));

        CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
            RegistryOps.of(JsonOps.INSTANCE, wrapperLookup), LOGGER::info, LOGGER::error);
    }

    public static void log(Level level, Object message) {
        LOGGER.log(level, "[Grind Enchantments] {}", message);
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}

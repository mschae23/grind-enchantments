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
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import de.mschae23.config.api.ConfigIo;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.legacy.v1.GrindEnchantmentsConfigV1;
import de.mschae23.grindenchantments.config.legacy.v2.GrindEnchantmentsConfigV2;
import de.mschae23.grindenchantments.config.legacy.GrindEnchantmentsConfigV3;
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

public class GrindEnchantmentsMod implements ModInitializer {
    public static final String MODID = "grindenchantments";
    public static final Logger LOGGER = LogManager.getLogger("Grind Enchantments");

    public static final Path CONFIG_PATH = Paths.get(MODID + ".json");

    private static GrindEnchantmentsConfigV3 LEGACY_CONFIG = GrindEnchantmentsConfigV3.DEFAULT;

    @Override
    public void onInitialize() {
        final GrindEnchantmentsConfigV3 legacyLatestConfigDefault = GrindEnchantmentsConfigV3.DEFAULT;
        final int legacyLatestConfigVersion = legacyLatestConfigDefault.version();
        @SuppressWarnings({"unchecked", "deprecation"})
        final MapCodec<? extends ModConfig<GrindEnchantmentsConfigV3>>[] legacyConfigCodecs = new MapCodec[] {
            GrindEnchantmentsConfigV1.TYPE_CODEC, GrindEnchantmentsConfigV2.TYPE_CODEC, GrindEnchantmentsConfigV3.TYPE_CODEC
        };

        final Codec<ModConfig<GrindEnchantmentsConfigV3>> legacyConfigCodec = ModConfig.createCodec(legacyLatestConfigVersion, version ->
            getConfigType(0, legacyConfigCodecs, version));

        // TODO Proper solution to client-side configs
        // ClientLifecycleEvents.CLIENT_STARTED.register(client ->
        //     CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
        //         JsonOps.INSTANCE, LOGGER::info, LOGGER::error)
        // );
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
            LEGACY_CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), legacyLatestConfigVersion, legacyLatestConfigDefault, legacyConfigCodec,
                RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), LOGGER::info, LOGGER::error)
        );

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

    private static <T extends ModConfig<T>> ModConfig.Type<T, ?> getConfigType(int versionOffset, MapCodec<? extends ModConfig<T>>[] codecs, int version) {
        for (int i = codecs.length; i > 0; i--) {
            if (version == i) {
                return new ModConfig.Type<>(i + versionOffset, codecs[i - 1]);
            }
        }

        return new ModConfig.Type<>(codecs.length + versionOffset, codecs[codecs.length - 1]);
    }

    public static GrindEnchantmentsConfigV3 getConfig() {
        return LEGACY_CONFIG;
    }

    public static void log(Level level, Object message) {
        LOGGER.log(level, "[Grind Enchantments] {}", message);
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}

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

package de.mschae23.grindenchantments.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;

public record GrindEnchantmentsV3Config(DisenchantConfig disenchant, MoveConfig move, ResetRepairCostConfig resetRepairCost,
                                        FilterConfig filter,
                                        DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) implements ModConfig<GrindEnchantmentsV3Config> {
    public static final MapCodec<GrindEnchantmentsV3Config> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsV3Config::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsV3Config::move),
        ResetRepairCostConfig.CODEC.fieldOf("reset_repair_cost").forGetter(GrindEnchantmentsV3Config::resetRepairCost),
        FilterConfig.CODEC.fieldOf("filter").forGetter(GrindEnchantmentsV3Config::filter),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsV3Config::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsV3Config::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsV3Config::new)));

    public static final ModConfig.Type<GrindEnchantmentsV3Config, GrindEnchantmentsV3Config> TYPE = new ModConfig.Type<>(3, TYPE_CODEC);

    public static final GrindEnchantmentsV3Config DEFAULT =
        new GrindEnchantmentsV3Config(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, ResetRepairCostConfig.DEFAULT, FilterConfig.DEFAULT, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);

    @Override
    public Type<GrindEnchantmentsV3Config, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsV3Config latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

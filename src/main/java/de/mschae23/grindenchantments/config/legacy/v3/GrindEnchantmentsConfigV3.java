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

package de.mschae23.grindenchantments.config.legacy.v3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.DisenchantConfig;
import de.mschae23.grindenchantments.config.MoveConfig;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public record GrindEnchantmentsConfigV3(DisenchantConfig disenchant, MoveConfig move, ResetRepairCostConfigV3 resetRepairCost,
                                        FilterConfigV3 filter,
                                        DedicatedServerConfigV3 dedicatedServerConfig, ClientConfigV3 clientConfig) implements ModConfig<GrindEnchantmentsConfigV3> {
    public static final MapCodec<GrindEnchantmentsConfigV3> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsConfigV3::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsConfigV3::move),
        ResetRepairCostConfigV3.CODEC.fieldOf("reset_repair_cost").forGetter(GrindEnchantmentsConfigV3::resetRepairCost),
        FilterConfigV3.CODEC.fieldOf("filter").forGetter(GrindEnchantmentsConfigV3::filter),
        DedicatedServerConfigV3.CODEC.orElse(DedicatedServerConfigV3.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsConfigV3::dedicatedServerConfig),
        ClientConfigV3.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsConfigV3::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsConfigV3::new)));

    public static final Type<GrindEnchantmentsConfigV3, GrindEnchantmentsConfigV3> TYPE = new Type<>(3, TYPE_CODEC);

    public static final GrindEnchantmentsConfigV3 DEFAULT =
        new GrindEnchantmentsConfigV3(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, ResetRepairCostConfigV3.DEFAULT, FilterConfigV3.DEFAULT, DedicatedServerConfigV3.DEFAULT, ClientConfigV3.DEFAULT);

    @Override
    public Type<GrindEnchantmentsConfigV3, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsConfigV3 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

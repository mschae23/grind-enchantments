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

package de.mschae23.grindenchantments.config.legacy.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.DisenchantConfig;
import de.mschae23.grindenchantments.config.FilterAction;
import de.mschae23.grindenchantments.config.MoveConfig;
import de.mschae23.grindenchantments.config.ResetRepairCostConfig;
import de.mschae23.grindenchantments.config.legacy.v3.ClientConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.DedicatedServerConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.FilterConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.GrindEnchantmentsConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.ResetRepairCostConfigV3;
import de.mschae23.grindenchantments.cost.FilterCostFunction;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record GrindEnchantmentsConfigV2(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                        DedicatedServerConfigV3 dedicatedServerConfig, ClientConfigV3 clientConfig) implements ModConfig<GrindEnchantmentsConfigV3> {
    public static final MapCodec<GrindEnchantmentsConfigV2> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsConfigV2::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsConfigV2::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsConfigV2::allowCurses),
        DedicatedServerConfigV3.CODEC.orElse(DedicatedServerConfigV3.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsConfigV2::dedicatedServerConfig),
        ClientConfigV3.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsConfigV2::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsConfigV2::new)));

    public static final Type<GrindEnchantmentsConfigV3, GrindEnchantmentsConfigV2> TYPE = new Type<>(2, TYPE_CODEC);

    public static final GrindEnchantmentsConfigV2 DEFAULT =
        new GrindEnchantmentsConfigV2(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfigV3.DEFAULT, ClientConfigV3.DEFAULT);

    @Override
    public Type<GrindEnchantmentsConfigV3, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsConfigV3 latest() {
        return new GrindEnchantmentsConfigV3(
            new DisenchantConfig(this.disenchant.enabled(), this.disenchant.consumeItem(),
                new FilterCostFunction(this.disenchant.costFunction())),
            new MoveConfig(this.move.enabled(),
                new FilterCostFunction(this.move.costFunction())),
            new ResetRepairCostConfigV3(false, ResetRepairCostConfigV3.DEFAULT.catalystItems(), ResetRepairCostConfig.DEFAULT.requiresEnchantment(), ResetRepairCostConfig.DEFAULT.costFunction()),
            new FilterConfigV3(true, FilterConfigV3.ItemConfig.DEFAULT, FilterConfigV3.EnchantmentConfig.DEFAULT,
                this.allowCurses ? FilterAction.ALLOW : FilterAction.IGNORE),
            this.dedicatedServerConfig, this.clientConfig);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

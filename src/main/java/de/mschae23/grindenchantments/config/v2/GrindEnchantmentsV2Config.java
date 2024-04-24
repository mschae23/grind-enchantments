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

package de.mschae23.grindenchantments.config.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.ClientConfig;
import de.mschae23.grindenchantments.config.DedicatedServerConfig;
import de.mschae23.grindenchantments.config.DisenchantConfig;
import de.mschae23.grindenchantments.config.FilterAction;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV3Config;
import de.mschae23.grindenchantments.config.MoveConfig;
import de.mschae23.grindenchantments.config.ResetRepairCostConfig;
import de.mschae23.grindenchantments.cost.FilterCostFunction;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record GrindEnchantmentsV2Config(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                        DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) implements ModConfig<GrindEnchantmentsV3Config> {
    public static final MapCodec<GrindEnchantmentsV2Config> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsV2Config::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsV2Config::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsV2Config::allowCurses),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsV2Config::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsV2Config::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsV2Config::new)));

    public static final Type<GrindEnchantmentsV3Config, GrindEnchantmentsV2Config> TYPE = new Type<>(2, TYPE_CODEC);

    public static final GrindEnchantmentsV2Config DEFAULT =
        new GrindEnchantmentsV2Config(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);

    @Override
    public Type<GrindEnchantmentsV3Config, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsV3Config latest() {
        return new GrindEnchantmentsV3Config(
            new DisenchantConfig(this.disenchant.enabled(), this.disenchant.consumeItem(),
                new FilterCostFunction(this.disenchant.costFunction())),
            new MoveConfig(this.move.enabled(),
                new FilterCostFunction(this.move.costFunction())),
            new ResetRepairCostConfig(false, ResetRepairCostConfig.DEFAULT.catalystItems(), ResetRepairCostConfig.DEFAULT.requiresEnchantment(), ResetRepairCostConfig.DEFAULT.costFunction()),
            new FilterConfig(true, FilterConfig.ItemConfig.DEFAULT, FilterConfig.EnchantmentConfig.DEFAULT,
                this.allowCurses ? FilterAction.ALLOW : FilterAction.IGNORE),
            this.dedicatedServerConfig, this.clientConfig);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

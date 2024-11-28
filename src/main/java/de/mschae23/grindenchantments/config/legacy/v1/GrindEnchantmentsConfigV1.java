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

package de.mschae23.grindenchantments.config.legacy.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.legacy.v2.GrindEnchantmentsConfigV2;
import de.mschae23.grindenchantments.config.legacy.v3.ClientConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.DedicatedServerConfigV3;
import de.mschae23.grindenchantments.config.legacy.v3.GrindEnchantmentsConfigV3;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record GrindEnchantmentsConfigV1(DisenchantConfigV1 disenchant, MoveConfigV1 move, boolean allowCurses,
                                        DedicatedServerConfigV3 dedicatedServerConfig, ClientConfigV3 clientConfig) implements ModConfig<GrindEnchantmentsConfigV3> {
    public static final MapCodec<GrindEnchantmentsConfigV1> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfigV1.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsConfigV1::disenchant),
        MoveConfigV1.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsConfigV1::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsConfigV1::allowCurses),
        DedicatedServerConfigV3.CODEC.orElse(DedicatedServerConfigV3.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsConfigV1::dedicatedServerConfig),
        ClientConfigV3.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsConfigV1::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsConfigV1::new)));

    public static final Type<GrindEnchantmentsConfigV3, GrindEnchantmentsConfigV1> TYPE = new Type<>(1, TYPE_CODEC);

    @Override
    public Type<GrindEnchantmentsConfigV3, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsConfigV3 latest() {
        return new GrindEnchantmentsConfigV2(this.disenchant.latest(), this.move.latest(), this.allowCurses, this.dedicatedServerConfig, this.clientConfig).latest();
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

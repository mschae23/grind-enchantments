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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.config.v1.GrindEnchantmentsV1Config;

public record GrindEnchantmentsV2Config(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                        DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) implements ModConfig<GrindEnchantmentsV2Config> {
    public static final MapCodec<GrindEnchantmentsV2Config> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsV2Config::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsV2Config::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsV2Config::allowCurses),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsV2Config::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsV2Config::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsV2Config::new)));

    public static final ModConfig.Type<GrindEnchantmentsV2Config, GrindEnchantmentsV2Config> TYPE = new ModConfig.Type<>(2, TYPE_CODEC);
    @SuppressWarnings({"unchecked", "deprecation"})
    public static final ModConfig.Type<GrindEnchantmentsV2Config, ? extends ModConfig<GrindEnchantmentsV2Config>>[] VERSIONS = new ModConfig.Type[] {
        GrindEnchantmentsV1Config.TYPE, GrindEnchantmentsV2Config.TYPE
    };

    public static final GrindEnchantmentsV2Config DEFAULT =
        new GrindEnchantmentsV2Config(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);

    @Override
    public Type<GrindEnchantmentsV2Config, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsV2Config latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}

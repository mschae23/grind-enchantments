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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import com.mojang.serialization.Codec;

public enum FilterAction implements StringIdentifiable {
    ALLOW("allow"),
    IGNORE("ignore"),
    DENY("deny");

    public static final Codec<FilterAction> CODEC = StringIdentifiable.createCodec(FilterAction::values);
    public static final Codec<FilterAction> NON_IGNORE_CODEC = StringIdentifiable.createCodec(() -> new FilterAction[] { ALLOW, DENY, });

    public static final PacketCodec<PacketByteBuf, FilterAction> PACKET_CODEC = PacketCodecs.indexed(i -> values()[i], FilterAction::ordinal).cast();

    private final String name;

    FilterAction(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}

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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DedicatedServerConfig(boolean alternativeCostDisplay) {
    public static final Codec<DedicatedServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("alternative_cost_display_enabled").forGetter(DedicatedServerConfig::alternativeCostDisplay)
    ).apply(instance, instance.stable(DedicatedServerConfig::new)));

    public static final PacketCodec<PacketByteBuf, DedicatedServerConfig> PACKET_CODEC = PacketCodecs.BOOL.xmap(
        DedicatedServerConfig::new, DedicatedServerConfig::alternativeCostDisplay).cast();

    public static final DedicatedServerConfig DEFAULT = new DedicatedServerConfig(false);
    public static final DedicatedServerConfig DISABLED = new DedicatedServerConfig(false);

    @Override
    public String toString() {
        return "DedicatedServerConfig{" +
            "alternativeCostDisplay=" + this.alternativeCostDisplay +
            '}';
    }
}

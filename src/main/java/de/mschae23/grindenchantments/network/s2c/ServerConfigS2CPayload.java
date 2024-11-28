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

package de.mschae23.grindenchantments.network.s2c;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;

public record ServerConfigS2CPayload() implements CustomPayload {
    public static final Identifier PACKET_ID = GrindEnchantmentsMod.id("server_config");
    public static final CustomPayload.Id<ServerConfigS2CPayload> ID = new CustomPayload.Id<>(PACKET_ID);

    public static final PacketCodec<PacketByteBuf, ServerConfigS2CPayload> CODEC = PacketCodec.unit(new ServerConfigS2CPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

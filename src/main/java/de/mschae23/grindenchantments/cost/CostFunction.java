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

package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.Codec;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;

public interface CostFunction {
    Codec<CostFunction> CODEC = GrindEnchantmentsRegistries.COST_FUNCTION.getCodec().dispatch(CostFunction::getType, CostFunctionType::codec);

    double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup);

    CostFunctionType<?> getType();

    static PacketCodec<PacketByteBuf, CostFunction> createPacketCodec() {
        return PacketCodec.recursive(delegateCodec ->
            CostFunctionType.createPacketCodec().<PacketByteBuf>cast().dispatch(CostFunction::getType,
                type -> type.packetCodec(delegateCodec)));
    }

    @Override
    String toString();
}

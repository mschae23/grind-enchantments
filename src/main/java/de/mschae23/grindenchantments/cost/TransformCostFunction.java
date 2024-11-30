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
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.FilterConfig;

public record TransformCostFunction(CostFunction function, double factor, double offset) implements CostFunction {
    public static final MapCodec<TransformCostFunction> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.CODEC.fieldOf("function").forGetter(TransformCostFunction::function),
        Codec.DOUBLE.fieldOf("factor").orElse(1.0).forGetter(TransformCostFunction::factor),
        Codec.DOUBLE.fieldOf("offset").orElse(0.0).forGetter(TransformCostFunction::offset)
    ).apply(instance, instance.stable(TransformCostFunction::new)));
    public static final CostFunctionType.Impl<TransformCostFunction> TYPE = new CostFunctionType.Impl<>(TYPE_CODEC, TransformCostFunction::packetCodec);

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        return (this.function.getCost(enchantments, filter, wrapperLookup)) * this.factor + this.offset;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.TRANSFORM;
    }

    public static PacketCodec<PacketByteBuf, TransformCostFunction> packetCodec(PacketCodec<PacketByteBuf, CostFunction> delegateCodec) {
        return PacketCodec.tuple(delegateCodec, TransformCostFunction::function, PacketCodecs.DOUBLE, TransformCostFunction::factor, PacketCodecs.DOUBLE, TransformCostFunction::offset, TransformCostFunction::new);
    }

    @Override
    public String toString() {
        return "TransformCostFunction{" +
            "function=" + this.function +
            ", factor=" + this.factor +
            ", offset=" + this.offset +
            '}';
    }
}

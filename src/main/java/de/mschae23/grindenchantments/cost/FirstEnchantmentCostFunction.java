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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.impl.MoveOperation;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public record FirstEnchantmentCostFunction(CostFunction function) implements CostFunction {
    public static final MapCodec<FirstEnchantmentCostFunction> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.CODEC.fieldOf("function").forGetter(FirstEnchantmentCostFunction::function)
    ).apply(instance, instance.stable(FirstEnchantmentCostFunction::new)));
    public static final CostFunctionType.Impl<FirstEnchantmentCostFunction> TYPE = new CostFunctionType.Impl<>(TYPE_CODEC, FirstEnchantmentCostFunction::packetCodec);

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = MoveOperation.getFirstEnchantment(enchantments, false, wrapperLookup);

        if (firstEnchantment == null) {
            return 1.0;
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(firstEnchantment.left(), firstEnchantment.rightInt());

            return this.function.getCost(builder.build(), filter, wrapperLookup);
        }
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.FIRST_ENCHANTMENT;
    }

    public static PacketCodec<PacketByteBuf, FirstEnchantmentCostFunction> packetCodec(PacketCodec<PacketByteBuf, CostFunction> delegateCodec) {
        return PacketCodec.tuple(delegateCodec, FirstEnchantmentCostFunction::function, FirstEnchantmentCostFunction::new);
    }

    @Override
    public String toString() {
        return "FirstEnchantmentCostFunction{" +
            "function=" + this.function +
            '}';
    }
}

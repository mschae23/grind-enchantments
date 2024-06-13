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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.impl.MoveOperation;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public record FirstEnchantmentCostFunction(CostFunction delegate) implements CostFunction {
    public static final MapCodec<FirstEnchantmentCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TYPE_CODEC.fieldOf("function").forGetter(FirstEnchantmentCostFunction::delegate)
    ).apply(instance, instance.stable(FirstEnchantmentCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = MoveOperation.getFirstEnchantment(enchantments, wrapperLookup);

        if (firstEnchantment == null) {
            return 1.0;
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(firstEnchantment.left(), firstEnchantment.rightInt());

            return this.delegate.getCost(builder.build(), filter, wrapperLookup);
        }
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.FIRST_ENCHANTMENT;
    }
}

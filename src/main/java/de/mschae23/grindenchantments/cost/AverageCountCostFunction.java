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
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AverageCountCostFunction(CostFunction function) implements CostFunction {
    public static final MapCodec<AverageCountCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(AverageCountCostFunction::function)
    ).apply(instance, instance.stable(AverageCountCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        double cost = this.function.getCost(enchantments, allowCurses, wrapperLookup);
        long count = enchantments.getEnchantments().stream().filter(entry -> allowCurses || !entry.value().isCursed()).count();

        if (count == 0) {
            return cost;
        }

        return cost / (double) count;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.AVERAGE_COUNT;
    }
}

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
import de.mschae23.grindenchantments.config.FilterConfig;

public record FilterCostFunction(CostFunction function) implements CostFunction {
    public static final MapCodec<FilterCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(FilterCostFunction::function)
    ).apply(instance, instance.stable(FilterCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.function.getCost(filter.filter(enchantments), filter, wrapperLookup);
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.FILTER;
    }
}

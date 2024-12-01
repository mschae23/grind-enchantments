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

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TransformCostFunction(CostFunction function, double factor, double offset) implements CostFunction {
    public static final Codec<TransformCostFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(TransformCostFunction::function),
        Codec.DOUBLE.fieldOf("factor").orElse(1.0).forGetter(TransformCostFunction::factor),
        Codec.DOUBLE.fieldOf("offset").orElse(0.0).forGetter(TransformCostFunction::offset)
    ).apply(instance, instance.stable(TransformCostFunction::new)));

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return (this.function.getCost(enchantments, allowCurses)) * this.factor + this.offset;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.TRANSFORM;
    }
}

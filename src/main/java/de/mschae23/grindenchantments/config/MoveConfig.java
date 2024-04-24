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

import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.FilterCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;
import de.mschae23.grindenchantments.cost.FirstEnchantmentCostFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MoveConfig(boolean enabled, CostFunction costFunction) {
    public static final Codec<MoveConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(MoveConfig::enabled),
        CostFunction.TYPE_CODEC.fieldOf("cost_function").forGetter(MoveConfig::costFunction)
    ).apply(instance, instance.stable(MoveConfig::new)));

    public static final MoveConfig DEFAULT = new MoveConfig(true,
        new FilterCostFunction(new FirstEnchantmentCostFunction(new TransformCostFunction(
            new CountLevelsCostFunction(3.0, 8.0), 0.5, 0.5))));
}

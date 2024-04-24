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

import net.minecraft.registry.Registry;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.MapCodec;

public interface CostFunctionType<M extends CostFunction> {
    CostFunctionType<CountEnchantmentsCostFunction> COUNT_ENCHANTMENTS = register("count_enchantments", CountEnchantmentsCostFunction.CODEC);
    CostFunctionType<CountLevelsCostFunction> COUNT_LEVELS = register("count_levels", CountLevelsCostFunction.CODEC);
    CostFunctionType<CountMinPowerCostFunction> COUNT_MIN_POWER = register("count_min_power", CountMinPowerCostFunction.CODEC);
    CostFunctionType<AverageCountCostFunction> AVERAGE_COUNT = register("average_count", AverageCountCostFunction.CODEC);
    CostFunctionType<FirstEnchantmentCostFunction> FIRST_ENCHANTMENT = register("first_enchantment", FirstEnchantmentCostFunction.CODEC);
    CostFunctionType<TransformCostFunction> TRANSFORM = register("transform", TransformCostFunction.CODEC);
    CostFunctionType<FilterCostFunction> FILTER = register("filter", FilterCostFunction.CODEC);

    MapCodec<M> codec();

    static <M extends CostFunction> CostFunctionType<M> register(String id, MapCodec<M> codec) {
        return Registry.register(GrindEnchantmentsRegistries.COST_FUNCTION, GrindEnchantmentsMod.id(id), () -> codec);
    }

    static void init() {
    }
}

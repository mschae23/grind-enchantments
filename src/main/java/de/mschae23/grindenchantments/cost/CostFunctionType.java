package de.mschae23.grindenchantments.cost;

import net.minecraft.registry.Registry;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.MapCodec;

public interface CostFunctionType<M extends CostFunction> {
    CostFunctionType<CountEnchantmentsCostFunction> COUNT_ENCHANTMENTS = register("count_enchantments", CountEnchantmentsCostFunction.CODEC);
    CostFunctionType<CountLevelsCostFunction> COUNT_LEVELS = register("count_levels", CountLevelsCostFunction.CODEC);
    CostFunctionType<CountMinPowerCostFunction> COUNT_MIN_POWER = register("count_min_power", CountMinPowerCostFunction.CODEC);
    CostFunctionType<FirstEnchantmentCostFunction> FIRST_ENCHANTMENT = register("first_enchantment", FirstEnchantmentCostFunction.CODEC);
    CostFunctionType<TransformCostFunction> TRANSFORM = register("transform", TransformCostFunction.CODEC);
    CostFunctionType<AverageCountCostFunction> AVERAGE_COUNT = register("average_count", AverageCountCostFunction.CODEC);

    MapCodec<M> codec();

    static <M extends CostFunction> CostFunctionType<M> register(String id, MapCodec<M> codec) {
        return Registry.register(GrindEnchantmentsRegistries.COST_FUNCTION, GrindEnchantmentsMod.id(id), () -> codec);
    }

    static void init() {
    }
}

package de.martenschaefer.grindenchantments.cost;

import net.minecraft.registry.Registry;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.Codec;

public interface CostFunctionType<M extends CostFunction> {
    CostFunctionType<CountEnchantmentsCostFunction> COUNT_ENCHANTMENTS = register("count_enchantments", CountEnchantmentsCostFunction.CODEC);
    CostFunctionType<CountLevelsCostFunction> COUNT_LEVELS = register("count_levels", CountLevelsCostFunction.CODEC);
    CostFunctionType<CountMinPowerCostFunction> COUNT_MIN_POWER = register("count_min_power", CountMinPowerCostFunction.CODEC);
    CostFunctionType<FirstEnchantmentCostFunction> FIRST_ENCHANTMENT = register("first_enchantment", FirstEnchantmentCostFunction.CODEC);
    CostFunctionType<TransformCostFunction> TRANSFORM = register("transform", TransformCostFunction.CODEC);
    CostFunctionType<AverageCountCostFunction> AVERAGE_COUNT = register("average_count", AverageCountCostFunction.CODEC);

    Codec<M> codec();

    static <M extends CostFunction> CostFunctionType<M> register(String id, Codec<M> codec) {
        return Registry.register(GrindEnchantmentsRegistries.COST_FUNCTION, GrindEnchantmentsMod.id(id), () -> codec);
    }

    static void init() {
    }
}

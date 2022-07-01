package de.martenschaefer.grindenchantments.cost;

import net.minecraft.util.registry.Registry;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.Codec;

public interface CostCountModeType<M extends CostCountMode> {
    CostCountModeType<CountEnchantmentsCostCountMode> COUNT_ENCHANTMENTS = register("count_enchantments", CountEnchantmentsCostCountMode.CODEC);
    CostCountModeType<CountLevelsCostCountMode> COUNT_LEVELS = register("count_levels", CountLevelsCostCountMode.CODEC);
    CostCountModeType<CountMinPowerCostCountMode> COUNT_MIN_POWER = register("count_min_power", CountMinPowerCostCountMode.CODEC);
    CostCountModeType<FirstEnchantmentCostCountMode> FIRST_ENCHANTMENT = register("first_enchantment", FirstEnchantmentCostCountMode.CODEC);

    Codec<M> codec();

    static <M extends CostCountMode> CostCountModeType<M> register(String id, Codec<M> codec) {
        return Registry.register(GrindEnchantmentsRegistries.COST_COUNT_MODE, GrindEnchantmentsMod.id(id), () -> codec);
    }

    static void init() {
    }
}

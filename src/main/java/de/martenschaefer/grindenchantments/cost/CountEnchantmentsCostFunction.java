package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountEnchantmentsCostFunction implements AbstractCostFunction {
    public static final CountEnchantmentsCostFunction INSTANCE = new CountEnchantmentsCostFunction();
    public static final Codec<CountEnchantmentsCostFunction> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + 1;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_ENCHANTMENTS;
    }
}

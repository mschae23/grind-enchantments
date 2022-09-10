package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountMinPowerCostFunction implements AbstractCostFunction {
    public static final CountMinPowerCostFunction INSTANCE = new CountMinPowerCostFunction();
    public static final Codec<CountMinPowerCostFunction> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + enchantment.getMinPower(level);
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_MIN_POWER;
    }
}

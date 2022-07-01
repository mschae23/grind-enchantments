package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountEnchantmentsCostCountMode implements AbstractCostCountMode {
    public static final CountEnchantmentsCostCountMode INSTANCE = new CountEnchantmentsCostCountMode();
    public static final Codec<CountEnchantmentsCostCountMode> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + 1;
    }

    @Override
    public CostCountModeType<?> getType() {
        return CostCountModeType.COUNT_ENCHANTMENTS;
    }
}

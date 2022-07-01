package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountMinPowerCostCountMode implements AbstractCostCountMode {
    public static final CountMinPowerCostCountMode INSTANCE = new CountMinPowerCostCountMode();
    public static final Codec<CountMinPowerCostCountMode> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + enchantment.getMinPower(level);
    }

    @Override
    public CostCountModeType<?> getType() {
        return CostCountModeType.COUNT_MIN_POWER;
    }
}

package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountMinPowerCostFunction implements CostFunction {
    public static final CountMinPowerCostFunction INSTANCE = new CountMinPowerCostFunction();
    public static final Codec<CountMinPowerCostFunction> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return enchantments.stream().filter(entry -> allowCurses || !entry.getKey().isCursed())
            .mapToDouble(entry -> entry.getKey().getMinPower(entry.getValue()))
            .sum();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_MIN_POWER;
    }
}

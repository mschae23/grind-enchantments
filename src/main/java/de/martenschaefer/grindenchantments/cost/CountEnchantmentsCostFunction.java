package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;

public class CountEnchantmentsCostFunction implements CostFunction {
    public static final CountEnchantmentsCostFunction INSTANCE = new CountEnchantmentsCostFunction();
    public static final Codec<CountEnchantmentsCostFunction> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return (double) enchantments.stream().filter(entry -> allowCurses || !entry.getKey().isCursed()).count();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_ENCHANTMENTS;
    }
}

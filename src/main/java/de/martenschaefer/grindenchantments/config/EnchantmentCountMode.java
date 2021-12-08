package de.martenschaefer.grindenchantments.config;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;

@FunctionalInterface
public interface EnchantmentCountMode {
    double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses);

    // Helper function for impl
    static double reduce(Set<Map.Entry<Enchantment, Integer>> enchantments, ReduceFunction function, boolean allowCurses) {
        double acc = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchantments) {
            Enchantment enchantment = entry.getKey();

            if (allowCurses || !enchantment.isCursed())
                acc = function.apply(acc, enchantment, entry.getValue());
        }

        return acc;
    }

    interface ReduceFunction {
        double apply(double accumulator, Enchantment enchantment, int level);
    }
}

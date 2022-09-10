package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;

public interface AbstractCostFunction extends CostFunction {
    double apply(double accumulator, Enchantment enchantment, int level);

    @Override
    default double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        double acc = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchantments) {
            Enchantment enchantment = entry.getKey();

            if (allowCurses || !enchantment.isCursed())
                acc = apply(acc, enchantment, entry.getValue());
        }

        return acc;
    }
}

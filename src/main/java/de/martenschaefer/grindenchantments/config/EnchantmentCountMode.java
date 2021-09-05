package de.martenschaefer.grindenchantments.config;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;

@FunctionalInterface
public interface EnchantmentCountMode {
    double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments);
}

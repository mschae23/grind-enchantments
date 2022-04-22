package de.martenschaefer.grindenchantments.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.StringIdentifiable;

public enum EnchantmentCountModes implements EnchantmentCountMode, StringIdentifiable {
    COUNT_ENCHANTMENTS("count_enchantments", (acc, enchantment, level) -> acc + 1),
    COUNT_LEVELS("count_levels", (acc, enchantment, level) -> acc + level);

    public static final com.mojang.serialization.Codec<EnchantmentCountModes> CODEC =
        StringIdentifiable.createCodec(EnchantmentCountModes::values);
    private static final Map<String, EnchantmentCountModes> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toMap(EnchantmentCountModes::getName, category -> category));

    private final String name;
    private final EnchantmentCountMode.ReduceFunction function;

    EnchantmentCountModes(String name, EnchantmentCountMode.ReduceFunction function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return EnchantmentCountMode.reduce(enchantments, this.function, allowCurses);
    }

    public static EnchantmentCountModes byName(String name) {
        return BY_NAME.get(name);
    }

    @Override
    public String asString() {
        return this.name;
    }
}

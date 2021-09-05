package de.martenschaefer.grindenchantments.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.StringIdentifiable;
import com.mojang.serialization.Codec;

public enum EnchantmentCountModes implements EnchantmentCountMode, StringIdentifiable {
    COUNT_ENCHANTMENTS("count_enchantments") {
        @Override
        public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments) {
            int i = 0;

            for (Map.Entry<Enchantment, Integer> entry : enchantments) {
                Enchantment enchantment = entry.getKey();

                if (!enchantment.isCursed()) i++;
            }

            return i;
        }
    },
    COUNT_LEVELS("count_levels") {
        @Override
        public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments) {
            int i = 0;

            for (Map.Entry<Enchantment, Integer> entry : enchantments) {
                Enchantment enchantment = entry.getKey();

                if (!enchantment.isCursed()) i += entry.getValue();
            }

            return i;
        }
    };

    public static final Codec<EnchantmentCountModes> CODEC =
        StringIdentifiable.createCodec(EnchantmentCountModes::values, EnchantmentCountModes::byName);
    private static final Map<String, EnchantmentCountModes> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toMap(EnchantmentCountModes::getName, category -> category));

    private final String name;

    EnchantmentCountModes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static EnchantmentCountModes byName(String name) {
        return BY_NAME.get(name);
    }

    @Override
    public String asString() {
        return this.name;
    }
}

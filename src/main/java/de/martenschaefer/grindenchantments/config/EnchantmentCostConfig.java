package de.martenschaefer.grindenchantments.config;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import de.martenschaefer.grindenchantments.cost.CostFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantmentCostConfig(CostFunction function, double factor, double offset) {
    public static final Codec<EnchantmentCostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("count_mode").forGetter(EnchantmentCostConfig::function),
        Codec.DOUBLE.fieldOf("cost_factor").orElse(1.0).forGetter(EnchantmentCostConfig::factor),
        Codec.DOUBLE.fieldOf("cost_offset").orElse(0.0).forGetter(EnchantmentCostConfig::offset)
    ).apply(instance, instance.stable(EnchantmentCostConfig::new)));

    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return this.function.getCost(enchantments, allowCurses) * this.factor + this.offset;
    }
}

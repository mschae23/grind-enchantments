package de.martenschaefer.grindenchantments.config;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import de.martenschaefer.grindenchantments.cost.CostCountMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantmentCostConfig(CostCountMode countMode, double costFactor, double costOffset) {
    public static final Codec<EnchantmentCostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostCountMode.TYPE_CODEC.fieldOf("count_mode").forGetter(EnchantmentCostConfig::countMode),
        Codec.DOUBLE.fieldOf("cost_factor").orElse(1.0).forGetter(EnchantmentCostConfig::costFactor),
        Codec.DOUBLE.fieldOf("cost_offset").orElse(0.0).forGetter(EnchantmentCostConfig::costOffset)
    ).apply(instance, instance.stable(EnchantmentCostConfig::new)));

    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return this.countMode.getCost(enchantments, allowCurses) * this.costFactor + this.costOffset;
    }
}

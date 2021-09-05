package de.martenschaefer.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantmentCostConfig(EnchantmentCountModes countMode, double costFactor, double costOffset) {
    public static final Codec<EnchantmentCostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnchantmentCountModes.CODEC.fieldOf("count_mode").forGetter(EnchantmentCostConfig::countMode),
        Codec.DOUBLE.fieldOf("cost_factor").forGetter(EnchantmentCostConfig::costFactor),
        Codec.DOUBLE.fieldOf("cost_offset").orElse(0.0).forGetter(EnchantmentCostConfig::costOffset)
    ).apply(instance, instance.stable(EnchantmentCostConfig::new)));
}

package de.martenschaefer.grindenchantments.config;

import de.martenschaefer.grindenchantments.cost.CountMinPowerCostCountMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DisenchantConfig(boolean enabled, boolean consumeItem, EnchantmentCostConfig costConfig) {
    public static final Codec<DisenchantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(DisenchantConfig::enabled),
        Codec.BOOL.fieldOf("consume_enchanted_item").forGetter(DisenchantConfig::consumeItem),
        EnchantmentCostConfig.CODEC.fieldOf("cost_config").forGetter(DisenchantConfig::costConfig)
    ).apply(instance, instance.stable(DisenchantConfig::new)));

    public static final DisenchantConfig DEFAULT = new DisenchantConfig(true, false,
        new EnchantmentCostConfig(new CountMinPowerCostCountMode(), 0.3, 8.0));
}

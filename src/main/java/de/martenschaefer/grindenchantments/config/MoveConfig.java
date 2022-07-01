package de.martenschaefer.grindenchantments.config;

import de.martenschaefer.grindenchantments.cost.CountLevelsCostCountMode;
import de.martenschaefer.grindenchantments.cost.FirstEnchantmentCostCountMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MoveConfig(boolean enabled, EnchantmentCostConfig costConfig) {
    public static final Codec<MoveConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(MoveConfig::enabled),
        EnchantmentCostConfig.CODEC.fieldOf("cost_config").forGetter(MoveConfig::costConfig)
    ).apply(instance, instance.stable(MoveConfig::new)));

    public static final MoveConfig DEFAULT = new MoveConfig(true,
        new EnchantmentCostConfig(new FirstEnchantmentCostCountMode(new CountLevelsCostCountMode(3.0, 8.0)), 0.5, 0.5));
}

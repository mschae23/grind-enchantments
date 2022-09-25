package de.martenschaefer.grindenchantments.config;

import de.martenschaefer.grindenchantments.cost.CostFunction;
import de.martenschaefer.grindenchantments.cost.CountMinPowerCostFunction;
import de.martenschaefer.grindenchantments.cost.TransformCostFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DisenchantConfig(boolean enabled, boolean consumeItem, CostFunction costFunction) {
    public static final Codec<DisenchantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(DisenchantConfig::enabled),
        Codec.BOOL.fieldOf("consume_enchanted_item").forGetter(DisenchantConfig::consumeItem),
        CostFunction.TYPE_CODEC.fieldOf("cost_function").forGetter(DisenchantConfig::costFunction)
    ).apply(instance, instance.stable(DisenchantConfig::new)));

    public static final DisenchantConfig DEFAULT = new DisenchantConfig(true, false,
        new TransformCostFunction(new CountMinPowerCostFunction(), 0.3, 8.0));
}

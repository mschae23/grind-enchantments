package de.mschae23.grindenchantments.config;

import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;
import de.mschae23.grindenchantments.cost.FirstEnchantmentCostFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MoveConfig(boolean enabled, CostFunction costFunction) {
    public static final Codec<MoveConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(MoveConfig::enabled),
        CostFunction.TYPE_CODEC.fieldOf("cost_function").forGetter(MoveConfig::costFunction)
    ).apply(instance, instance.stable(MoveConfig::new)));

    public static final MoveConfig DEFAULT = new MoveConfig(true,
        new TransformCostFunction(new FirstEnchantmentCostFunction(new CountLevelsCostFunction(3.0, 8.0)), 0.5, 0.5));
}

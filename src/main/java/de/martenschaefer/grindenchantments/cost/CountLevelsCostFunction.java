package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CountLevelsCostFunction(double normalFactor, double treasureFactor) implements AbstractCostFunction {
    public static final Codec<CountLevelsCostFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("normal_factor").forGetter(CountLevelsCostFunction::normalFactor),
        Codec.DOUBLE.fieldOf("treasure_factor").forGetter(CountLevelsCostFunction::treasureFactor)
    ).apply(instance, instance.stable(CountLevelsCostFunction::new)));

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + (enchantment.isTreasure() ? level * this.treasureFactor : level * this.normalFactor);
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_LEVELS;
    }
}

package de.martenschaefer.grindenchantments.cost;

import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CountLevelsCostCountMode(double normalFactor, double treasureFactor) implements AbstractCostCountMode {
    public static final Codec<CountLevelsCostCountMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("normal_factor").forGetter(CountLevelsCostCountMode::normalFactor),
        Codec.DOUBLE.fieldOf("treasure_factor").forGetter(CountLevelsCostCountMode::treasureFactor)
    ).apply(instance, instance.stable(CountLevelsCostCountMode::new)));

    @Override
    public double apply(double accumulator, Enchantment enchantment, int level) {
        return accumulator + (enchantment.isTreasure() ? level * this.treasureFactor : level * this.normalFactor);
    }

    @Override
    public CostCountModeType<?> getType() {
        return CostCountModeType.COUNT_LEVELS;
    }
}

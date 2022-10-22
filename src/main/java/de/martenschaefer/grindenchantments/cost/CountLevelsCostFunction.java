package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CountLevelsCostFunction(double normalFactor, double treasureFactor) implements CostFunction {
    public static final Codec<CountLevelsCostFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("normal_factor").forGetter(CountLevelsCostFunction::normalFactor),
        Codec.DOUBLE.fieldOf("treasure_factor").forGetter(CountLevelsCostFunction::treasureFactor)
    ).apply(instance, instance.stable(CountLevelsCostFunction::new)));

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return enchantments.stream().filter(entry -> allowCurses || !entry.getKey().isCursed())
            .mapToDouble(entry -> (double) entry.getValue() * (entry.getKey().isTreasure() ? this.treasureFactor : this.normalFactor))
            .sum();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_LEVELS;
    }
}

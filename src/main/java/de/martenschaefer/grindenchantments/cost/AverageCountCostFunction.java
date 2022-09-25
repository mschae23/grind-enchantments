package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AverageCountCostFunction(CostFunction function) implements CostFunction {
    public static final Codec<AverageCountCostFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(AverageCountCostFunction::function)
    ).apply(instance, instance.stable(AverageCountCostFunction::new)));

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        double cost = this.function.getCost(enchantments, allowCurses);

        long count = enchantments.stream().filter(entry -> allowCurses || !entry.getKey().isCursed()).count();

        if (count == 0) {
            return cost;
        }

        return cost / (double) count;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.AVERAGE_COUNT;
    }
}

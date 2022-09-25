package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TransformCostFunction(CostFunction function, double factor, double offset) implements CostFunction {
    public static final Codec<TransformCostFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(TransformCostFunction::function),
        Codec.DOUBLE.fieldOf("factor").orElse(1.0).forGetter(TransformCostFunction::factor),
        Codec.DOUBLE.fieldOf("offset").orElse(0.0).forGetter(TransformCostFunction::offset)
    ).apply(instance, instance.stable(TransformCostFunction::new)));

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        return (this.function.getCost(enchantments, allowCurses)) * this.factor + this.offset;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.TRANSFORM;
    }
}

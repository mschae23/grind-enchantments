package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TransformCostFunction(CostFunction function, double factor, double offset) implements CostFunction {
    public static final MapCodec<TransformCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TYPE_CODEC.fieldOf("function").forGetter(TransformCostFunction::function),
        Codec.DOUBLE.fieldOf("factor").orElse(1.0).forGetter(TransformCostFunction::factor),
        Codec.DOUBLE.fieldOf("offset").orElse(0.0).forGetter(TransformCostFunction::offset)
    ).apply(instance, instance.stable(TransformCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        return (this.function.getCost(enchantments, allowCurses, wrapperLookup)) * this.factor + this.offset;
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.TRANSFORM;
    }
}

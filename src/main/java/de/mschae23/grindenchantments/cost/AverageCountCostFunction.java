package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AverageCountCostFunction(CostFunction function) implements CostFunction {
    public static final MapCodec<AverageCountCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.TYPE_CODEC.fieldOf("function").forGetter(AverageCountCostFunction::function)
    ).apply(instance, instance.stable(AverageCountCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        double cost = this.function.getCost(enchantments, allowCurses, wrapperLookup);
        long count = enchantments.getEnchantments().stream().filter(entry -> allowCurses || !entry.value().isCursed()).count();

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

package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CountLevelsCostFunction(double normalFactor, double treasureFactor) implements CostFunction {
    public static final MapCodec<CountLevelsCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.DOUBLE.fieldOf("normal_factor").forGetter(CountLevelsCostFunction::normalFactor),
        Codec.DOUBLE.fieldOf("treasure_factor").forGetter(CountLevelsCostFunction::treasureFactor)
    ).apply(instance, instance.stable(CountLevelsCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        return enchantments.getEnchantmentsMap().stream()
            .filter(entry -> allowCurses || !entry.getKey().value().isCursed())
            .mapToDouble(entry -> (double) entry.getIntValue() * (entry.getKey().value().isTreasure() ? this.treasureFactor : this.normalFactor))
            .sum();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_LEVELS;
    }
}

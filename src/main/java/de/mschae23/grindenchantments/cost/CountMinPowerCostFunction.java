package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.MapCodec;

public class CountMinPowerCostFunction implements CostFunction {
    public static final CountMinPowerCostFunction INSTANCE = new CountMinPowerCostFunction();
    public static final MapCodec<CountMinPowerCostFunction> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        return enchantments.getEnchantmentsMap().stream()
            .filter(entry -> allowCurses || !entry.getKey().value().isCursed())
            .mapToDouble(entry -> entry.getKey().value().getMinPower(entry.getIntValue()))
            .sum();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_MIN_POWER;
    }
}

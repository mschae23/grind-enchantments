package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.MapCodec;

public class CountEnchantmentsCostFunction implements CostFunction {
    public static final CountEnchantmentsCostFunction INSTANCE = new CountEnchantmentsCostFunction();
    public static final MapCodec<CountEnchantmentsCostFunction> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        return (double) enchantments.getEnchantments().stream().filter(entry -> allowCurses || !entry.value().isCursed()).count();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_ENCHANTMENTS;
    }
}

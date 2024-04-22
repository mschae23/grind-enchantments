package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.Codec;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;

public interface CostFunction {
    Codec<CostFunction> TYPE_CODEC = GrindEnchantmentsRegistries.COST_FUNCTION.getCodec().dispatch(CostFunction::getType, CostFunctionType::codec);

    double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup);

    CostFunctionType<?> getType();
}

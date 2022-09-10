package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import de.martenschaefer.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.Codec;

public interface CostFunction {
    Codec<CostFunction> TYPE_CODEC = GrindEnchantmentsRegistries.COST_FUNCTION.getCodec().dispatch(CostFunction::getType, CostFunctionType::codec);

    double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses);

    CostFunctionType<?> getType();
}

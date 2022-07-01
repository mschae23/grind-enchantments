package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import de.martenschaefer.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.Codec;

public interface CostCountMode {
    Codec<CostCountMode> TYPE_CODEC = GrindEnchantmentsRegistries.COST_COUNT_MODE.getCodec().dispatch(CostCountMode::getType, CostCountModeType::codec);

    double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses);

    CostCountModeType<?> getType();
}

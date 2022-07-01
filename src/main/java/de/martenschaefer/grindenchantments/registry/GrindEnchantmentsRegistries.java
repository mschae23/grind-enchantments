package de.martenschaefer.grindenchantments.registry;

import net.minecraft.util.registry.Registry;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.cost.CostCountModeType;
import de.martenschaefer.grindenchantments.util.FabricRegistryBuilderUtil;

public final class GrindEnchantmentsRegistries {
    public static final Registry<CostCountModeType<?>> COST_COUNT_MODE = FabricRegistryBuilderUtil.<CostCountModeType<?>>createSimple(GrindEnchantmentsMod.id("cost_count_mode_type")).buildAndRegister();

    private GrindEnchantmentsRegistries() {
    }

    public static void init() {
    }
}

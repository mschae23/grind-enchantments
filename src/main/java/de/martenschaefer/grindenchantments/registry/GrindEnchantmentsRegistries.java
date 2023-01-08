package de.martenschaefer.grindenchantments.registry;

import net.minecraft.registry.Registry;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.cost.CostFunctionType;
import de.martenschaefer.grindenchantments.util.FabricRegistryBuilderUtil;

public final class GrindEnchantmentsRegistries {
    public static final Registry<CostFunctionType<?>> COST_FUNCTION = FabricRegistryBuilderUtil.<CostFunctionType<?>>createSimple(GrindEnchantmentsMod.id("cost_function_type")).buildAndRegister();

    private GrindEnchantmentsRegistries() {
    }

    public static void init() {
    }
}

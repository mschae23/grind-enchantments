package de.mschae23.grindenchantments.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.cost.CostFunctionType;

public final class GrindEnchantmentsRegistries {
    public static final Registry<CostFunctionType<?>> COST_FUNCTION = FabricRegistryBuilder.<CostFunctionType<?>>createSimple(RegistryKey.ofRegistry(GrindEnchantmentsMod.id("cost_function_type"))).buildAndRegister();

    private GrindEnchantmentsRegistries() {
    }

    public static void init() {
    }
}

package de.martenschaefer.grindenchantments.registry;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.cost.CostFunctionType;
import com.mojang.serialization.Lifecycle;

public final class GrindEnchantmentsRegistries {
    public static final Registry<CostFunctionType<?>> COST_FUNCTION = FabricRegistryBuilder.from(new SimpleRegistry<CostFunctionType<?>>(RegistryKey.ofRegistry(GrindEnchantmentsMod.id("cost_function_type")), Lifecycle.stable(), null)).buildAndRegister();

    private GrindEnchantmentsRegistries() {
    }

    public static void init() {
    }
}

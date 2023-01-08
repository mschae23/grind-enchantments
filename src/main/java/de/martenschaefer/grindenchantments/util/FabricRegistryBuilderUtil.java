package de.martenschaefer.grindenchantments.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import com.mojang.serialization.Lifecycle;

public final class FabricRegistryBuilderUtil {
    private FabricRegistryBuilderUtil() {
    }

    // Methods without Class<T> parameter

    /**
     * Create a new {@link FabricRegistryBuilder} using a {@link SimpleRegistry}, the registry has the {@link RegistryAttribute#MODDED} attribute by default.
     *
     * @param registryId The registry {@link Identifier} used as the registry id
     * @param <T> The type stored in the Registry
     * @return An instance of FabricRegistryBuilder
     */
    public static <T> FabricRegistryBuilder<T, SimpleRegistry<T>> createSimple(Identifier registryId) {
        return FabricRegistryBuilder.from(new SimpleRegistry<T>(RegistryKey.ofRegistry(registryId), Lifecycle.stable(), false));
    }
}

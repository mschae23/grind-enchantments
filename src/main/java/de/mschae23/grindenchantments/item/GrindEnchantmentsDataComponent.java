package de.mschae23.grindenchantments.item;

import net.minecraft.component.DataComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GrindEnchantmentsDataComponent(int cost, boolean canTake) {
    public static final Codec<GrindEnchantmentsDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("cost").forGetter(GrindEnchantmentsDataComponent::cost),
        Codec.BOOL.fieldOf("can_take").forGetter(GrindEnchantmentsDataComponent::canTake)
    ).apply(instance, instance.stable(GrindEnchantmentsDataComponent::new)));

    public static final DataComponentType<GrindEnchantmentsDataComponent> TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE,
        GrindEnchantmentsMod.id("grind_enchantments"), DataComponentType.<GrindEnchantmentsDataComponent>builder().codec(CODEC).build());

    public static void init() {
    }
}

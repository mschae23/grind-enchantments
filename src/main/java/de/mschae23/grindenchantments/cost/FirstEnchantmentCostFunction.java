package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.impl.MoveOperation;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public record FirstEnchantmentCostFunction(CostFunction delegate) implements CostFunction {
    public static final MapCodec<FirstEnchantmentCostFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        TYPE_CODEC.fieldOf("function").forGetter(FirstEnchantmentCostFunction::delegate)
    ).apply(instance, instance.stable(FirstEnchantmentCostFunction::new)));

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = MoveOperation.getFirstEnchantment(enchantments, allowCurses, wrapperLookup);

        if (firstEnchantment == null) {
            return 1.0;
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(firstEnchantment.left().value(), firstEnchantment.rightInt());

            return this.delegate.getCost(builder.build(), allowCurses, wrapperLookup);
        }
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.FIRST_ENCHANTMENT;
    }
}

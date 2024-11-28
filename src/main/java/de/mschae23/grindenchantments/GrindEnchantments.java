/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Grind enchantments.
 *
 * Grind enchantments is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.grindenchantments;

import java.util.function.IntSupplier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.config.legacy.v3.DedicatedServerConfigV3;
import de.mschae23.grindenchantments.cost.CostFunction;

public class GrindEnchantments {
    public static int getLevelCost(ItemStack stack, CostFunction costFunction, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        double cost = costFunction.getCost(enchantments, filter, wrapperLookup);

        return (int) Math.ceil(cost);
    }

    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack, FilterConfig filter) {
        return filter.filter(EnchantmentHelper.getEnchantments(stack));
    }

    public static ItemStack addLevelCostComponent(ItemStack stack, IntSupplier cost, boolean canTakeItem, DedicatedServerConfigV3 config) {
        if (!config.alternativeCostDisplay())
            return stack;

        ItemStack changed = stack.copy();
        changed.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(compound -> {
            NbtCompound tag = new NbtCompound();
            tag.putInt("Cost", cost.getAsInt());
            tag.putBoolean("CanTake", canTakeItem);

            compound.put(GrindEnchantmentsMod.MODID, tag);
        }));
        return changed;
    }

    public static ItemStack addLevelCostLore(ItemStack stack, IntSupplier cost, boolean canTakeItem) {
        MutableText text = Text.literal("Enchantment cost: " + cost.getAsInt())
            .formatted(canTakeItem ? Formatting.GREEN : Formatting.RED);

        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lore -> lore.with(text));
        return stack;
    }

    /**
     * @param stack mutable; same instance will be returned
     * @return the {@code stack} argument
     */
    @SuppressWarnings("UnusedReturnValue")
    public static ItemStack removeLevelCostNbt(ItemStack stack) {
        // Relies on ItemStacks being mutable AND the stack not being copied into the player inventory before calling this method

        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(compound ->
            compound.remove(GrindEnchantmentsMod.MODID)));
        return stack;
    }
}

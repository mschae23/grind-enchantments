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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.mschae23.grindenchantments.config.DedicatedServerConfig;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.item.GrindEnchantmentsDataComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class GrindEnchantments {
    public static int getLevelCost(ItemStack stack, CostFunction costFunction, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        double cost = costFunction.getCost(enchantments, allowCurses, wrapperLookup);

        return (int) Math.ceil(cost);
    }

    public static Object2IntMap<RegistryEntry<Enchantment>> getEnchantments(ItemStack stack, boolean allowCurses) {
        Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>(EnchantmentHelper.getEnchantments(stack).enchantments);

        if (!allowCurses) {
            // Don't transfer curses if it isn't enabled in the config
            for (RegistryEntry<Enchantment> entry : enchantments.keySet()) {
                if (entry.value().isCursed()) {
                    enchantments.removeInt(entry);
                }
            }
        }

        return enchantments;
    }

    public static ItemStack addLevelCostComponent(ItemStack stack, IntSupplier cost, boolean canTakeItem, DedicatedServerConfig config) {
        if (!config.alternativeCostDisplay())
            return stack;

        ItemStack changed = stack.copy();
        changed.set(GrindEnchantmentsDataComponent.TYPE, new GrindEnchantmentsDataComponent(cost.getAsInt(), canTakeItem));
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

        stack.remove(GrindEnchantmentsDataComponent.TYPE);
        return stack;
    }
}

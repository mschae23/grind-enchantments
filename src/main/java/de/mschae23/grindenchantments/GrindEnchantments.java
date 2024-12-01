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

import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.mschae23.grindenchantments.config.DedicatedServerConfig;
import de.mschae23.grindenchantments.cost.CostFunction;
import org.apache.logging.log4j.Level;

public class GrindEnchantments {
    public static int getLevelCost(ItemStack stack, CostFunction costFunction, boolean allowCurses) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        double cost = costFunction.getCost(enchantments.entrySet(), allowCurses);

        return (int) Math.ceil(cost);
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack, boolean allowCurses) {
        Stream<Map.Entry<Enchantment, Integer>> enchantments = EnchantmentHelper.get(stack).entrySet().stream();

        if (!allowCurses) // Don't transfer curses if it isn't enabled in the config
            enchantments = enchantments.filter(entry -> !entry.getKey().isCursed());

        return enchantments.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static ItemStack addLevelCostNbt(ItemStack stack, IntSupplier cost, boolean canTakeItem, DedicatedServerConfig config) {
        if (!config.alternativeCostDisplay())
            return stack;

        ItemStack changed = stack.copy();

        NbtCompound modNbt = changed.getOrCreateSubNbt(GrindEnchantmentsMod.MODID);
        modNbt.putInt("Cost", cost.getAsInt());
        modNbt.putBoolean("CanTake", canTakeItem);

        return changed;
    }

    public static ItemStack addLevelCostLore(ItemStack stack, IntSupplier cost, boolean canTakeItem) {
        ItemStack changed = stack.copy();

        NbtCompound display = changed.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        NbtList lore;

        if (display.getType(ItemStack.LORE_KEY) == NbtElement.LIST_TYPE) {
            lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        } else if (!display.contains(ItemStack.LORE_KEY)) {
            lore = new NbtList();
            display.put(ItemStack.LORE_KEY, lore);
        } else {
            GrindEnchantmentsMod.log(Level.ERROR, "Cannot add enchantment cost to item stack as lore.");
            return stack;
        }

        MutableText text = Text.literal("Enchantment cost: " + cost.getAsInt())
            .formatted(canTakeItem ? Formatting.GREEN : Formatting.RED);

        lore.add(NbtString.of(Text.Serializer.toJson(text)));
        return changed;
    }

    /**
     * @param stack mutable; same instance will be returned
     * @return the {@code stack} argument
     */
    @SuppressWarnings("UnusedReturnValue")
    public static ItemStack removeLevelCostNbt(ItemStack stack) {
        // Relies on ItemStacks being mutable AND the stack not being copied into the player inventory before calling this method

        stack.removeSubNbt(GrindEnchantmentsMod.MODID);
        return stack;
    }
}

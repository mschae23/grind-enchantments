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

package de.mschae23.grindenchantments.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents.CanInsert;
import de.mschae23.grindenchantments.event.GrindstoneEvents.CanTakeResult;
import de.mschae23.grindenchantments.event.GrindstoneEvents.LevelCost;
import de.mschae23.grindenchantments.event.GrindstoneEvents.TakeResult;
import de.mschae23.grindenchantments.event.GrindstoneEvents.UpdateResult;
import org.jetbrains.annotations.Nullable;

public class MoveOperation implements CanInsert, UpdateResult, CanTakeResult, TakeResult, LevelCost {
    @Override
    public boolean canInsert(ItemStack stack, ItemStack other, int slotId) {
        return stack.getItem() == Items.BOOK && !other.isOf(Items.BOOK);
    }

    @Override
    public @Nullable ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (!isMoveOperation(input1, input2)) {
            return null;
        }

        boolean allowCurses = GrindEnchantmentsMod.getConfig().allowCurses();

        List<Map.Entry<Enchantment, Integer>> firstEnchantments = EnchantmentHelper.get(input1).entrySet().stream()
            .filter(entry -> allowCurses || !entry.getKey().isCursed()).limit(2).toList();

        if (firstEnchantments.size() < 2)
            return null;

        Map.Entry<Enchantment, Integer> entry = firstEnchantments.get(0);
        ItemStack result;

        if (input2.getItem() == Items.ENCHANTED_BOOK) {
            Map<Enchantment, Integer> stack2 = EnchantmentHelper.get(input2);
            int level = entry.getValue();

            if (stack2.containsKey(entry.getKey())) {
                level = stack2.get(entry.getKey());

                if (level != entry.getValue() || entry.getValue() == entry.getKey().getMaxLevel())
                    return ItemStack.EMPTY;

                level += 1;
            }

            result = input2.copy();
            EnchantedBookItem.addEnchantment(result, new EnchantmentLevelEntry(entry.getKey(), level));
        } else {
            result = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    @Override
    public boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (isMoveOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            return canTakeResult(input1, input2, () ->
                GrindEnchantments.getLevelCost(input1, config.move().costFunction(), config.allowCurses()), player);
        }

        return true;
    }

    @Override
    public boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input) {
        if (!isMoveOperation(input1, input2)) {
            return false;
        }

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
        boolean allowCurses = config.allowCurses();

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(input1);
        Optional<Map.Entry<Enchantment, Integer>> enchantmentOptional = enchantments.entrySet().stream().filter(entry ->
            allowCurses || !entry.getKey().isCursed()).findFirst();

        if (enchantmentOptional.isEmpty())
            return false;

        Map.Entry<Enchantment, Integer> enchantment = enchantmentOptional.get();
        enchantments.remove(enchantment.getKey(), enchantment.getValue());

        ItemStack newEnchantedStack = new ItemStack(Items.ENCHANTED_BOOK);

        EnchantmentHelper.set(enchantments, newEnchantedStack);
        input.setStack(0, newEnchantedStack);

        if (input2.getItem() == Items.ENCHANTED_BOOK || input2.getCount() == 1)
            input.setStack(1, ItemStack.EMPTY);
        else {
            ItemStack newBookStack = input2.copy();
            newBookStack.setCount(input2.getCount() - 1);
            input.setStack(1, newBookStack);
        }

        if (!player.getAbilities().creativeMode) {
            int cost = GrindEnchantments.getLevelCost(input1, config.move().costFunction(), allowCurses);
            ApplyLevelCostEvent.EVENT.invoker().applyLevelCost(cost, player);
        }

        return true;
    }

    @Override
    public int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (isMoveOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            return GrindEnchantments.getLevelCost(input1, config.move().costFunction(), config.allowCurses());
        }

        return -1;
    }

    public static boolean isMoveOperation(ItemStack input1, ItemStack input2) {
        if (!GrindEnchantmentsMod.getConfig().move().enabled())
            return false;

        return input1.getItem() == Items.ENCHANTED_BOOK &&
            (input2.getItem() == Items.ENCHANTED_BOOK ||
                input2.getItem() == Items.BOOK);
    }

    public static boolean canTakeResult(@SuppressWarnings("unused") ItemStack input1, @SuppressWarnings("unused") ItemStack input2,
                                        IntSupplier cost, PlayerEntity player) {
        return player.getAbilities().creativeMode || player.experienceLevel >= cost.getAsInt();
    }
}

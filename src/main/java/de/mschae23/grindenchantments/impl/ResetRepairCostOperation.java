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

import java.util.function.IntSupplier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.FilterAction;
import de.mschae23.grindenchantments.config.FilterConfig;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV3Config;
import de.mschae23.grindenchantments.config.ResetRepairCostConfig;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import org.jetbrains.annotations.NotNull;

public class ResetRepairCostOperation implements GrindstoneEvents.CanInsert, GrindstoneEvents.UpdateResult, GrindstoneEvents.CanTakeResult, GrindstoneEvents.TakeResult, GrindstoneEvents.LevelCost {
    @Override
    public boolean canInsert(ItemStack stack, ItemStack other, int slotId) {
        ResetRepairCostConfig config = GrindEnchantmentsMod.getConfig().resetRepairCost();

        return config.enabled() && slotId == 1 && config.catalystItems().contains(stack.getRegistryEntry());
    }

    @Override
    public @NotNull ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isResetRepairCostOperation(input1, input2)) {
            return ItemStack.EMPTY;
        }

        GrindEnchantmentsV3Config config = GrindEnchantmentsMod.getConfig();
        FilterConfig filter = config.filter();

        if (filter.enabled() && filter.item().action() != FilterAction.IGNORE
            && (filter.item().action() == FilterAction.DENY) == filter.item().items().contains(input1.getRegistryEntry())) {
            return ItemStack.EMPTY;
        }

        if (input1.getOrDefault(DataComponentTypes.REPAIR_COST, 0) <= 0) {
            return ItemStack.EMPTY;
        } else if (config.resetRepairCost().requiresEnchantment() && !EnchantmentHelper.hasEnchantments(input1)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = input1.copy();
        result.remove(DataComponentTypes.REPAIR_COST);

        return result;
    }

    @Override
    public boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isResetRepairCostOperation(input1, input2)) {
            GrindEnchantmentsV3Config config = GrindEnchantmentsMod.getConfig();

            return canTakeResult(input1, input2, () ->
                GrindEnchantments.getLevelCost(input1, config.resetRepairCost().costFunction(), config.filter(), wrapperLookup), player);
        }

        return true;
    }

    @Override
    public boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isResetRepairCostOperation(input1, input2)) {
            return false;
        }

        GrindEnchantmentsV3Config config = GrindEnchantmentsMod.getConfig();
        FilterConfig filter = config.filter();

        input.setStack(0, ItemStack.EMPTY);

        if (input2.getCount() == 1)
            input.setStack(1, ItemStack.EMPTY);
        else {
            ItemStack newCatalystStack = input2.copy();
            newCatalystStack.setCount(input2.getCount() - 1);
            input.setStack(1, newCatalystStack);
        }

        if (!player.getAbilities().creativeMode) {
            int cost = GrindEnchantments.getLevelCost(input1, config.resetRepairCost().costFunction(), filter, wrapperLookup);
            ApplyLevelCostEvent.EVENT.invoker().applyLevelCost(cost, player);
        }

        return true;
    }

    @Override
    public int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isResetRepairCostOperation(input1, input2)) {
            GrindEnchantmentsV3Config config = GrindEnchantmentsMod.getConfig();

            return GrindEnchantments.getLevelCost(input1, config.resetRepairCost().costFunction(), config.filter(), wrapperLookup);
        }

        return -1;
    }

    public static boolean isResetRepairCostOperation(ItemStack input1, ItemStack input2) {
        ResetRepairCostConfig config = GrindEnchantmentsMod.getConfig().resetRepairCost();

        if (!config.enabled())
            return false;

        return (input1.isDamageable() || EnchantmentHelper.canHaveEnchantments(input1))
            && !input2.isOf(Items.BOOK) && !input2.isOf(Items.ENCHANTED_BOOK) && !input2.isDamageable() && !input2.isOf(input1.getItem())
            && config.catalystItems().contains(input2.getRegistryEntry());
    }

    public static boolean canTakeResult(@SuppressWarnings("unused") ItemStack input1, @SuppressWarnings("unused") ItemStack input2,
                                        IntSupplier cost, PlayerEntity player) {
        return player.getAbilities().creativeMode || player.experienceLevel >= cost.getAsInt();
    }
}

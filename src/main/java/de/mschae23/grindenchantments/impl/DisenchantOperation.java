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
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

public class DisenchantOperation implements GrindstoneEvents.CanInsert, GrindstoneEvents.UpdateResult, GrindstoneEvents.CanTakeResult, GrindstoneEvents.TakeResult, GrindstoneEvents.LevelCost {
    @Override
    public boolean canInsert(ItemStack stack, ItemStack other, int slotId) {
        return stack.getItem() == Items.BOOK && !other.isOf(Items.BOOK);
    }

    @Override
    public @NotNull ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isDisenchantOperation(input1, input2)) {
            return ItemStack.EMPTY;
        }

        ItemStack enchantedItemStack = input1.hasEnchantments() ? input1 : input2;

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
        return transferEnchantmentsToBook(enchantedItemStack, config.allowCurses());
    }

    @Override
    public boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isDisenchantOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            boolean stack1Book = input1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? input2 : input1;

            return canTakeResult(input1, input2, () ->
                GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses(), wrapperLookup), player);
        }

        return true;
    }

    @Override
    public boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isDisenchantOperation(input1, input2)) {
            return false;
        }

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

        boolean stack1Book = input1.getItem() == Items.BOOK;
        ItemStack enchantedItemStack = stack1Book ? input2 : input1;
        ItemStack bookItemStack = stack1Book ? input1 : input2;

        if (!player.getAbilities().creativeMode) {
            int cost = debugLevelCost("onTakeResult", GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses(), wrapperLookup));
            ApplyLevelCostEvent.EVENT.invoker().applyLevelCost(cost, player);
        }

        input.setStack(stack1Book ? 1 : 0, config.disenchant().consumeItem() ?
            ItemStack.EMPTY : grind(enchantedItemStack, config.allowCurses()));

        if (bookItemStack.getCount() == 1)
            input.setStack(stack1Book ? 0 : 1, ItemStack.EMPTY);
        else {
            ItemStack bookNew = bookItemStack.copy();
            bookNew.setCount(bookItemStack.getCount() - 1);
            input.setStack(stack1Book ? 0 : 1, bookNew);
        }

        return true;
    }

    @Override
    public int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isDisenchantOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
            boolean stack1Book = input1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? input2 : input1;

            return debugLevelCost("getLevelCost", GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses(), wrapperLookup));
        }

        return -1;
    }

    private static int debugLevelCost(@SuppressWarnings("unused") String location, int cost) {
        // GrindEnchantmentsMod.log(Level.INFO, "Level cost (" + location + "): " + cost);
        return cost;
    }

    public static boolean isDisenchantOperation(ItemStack input1, ItemStack input2) {
        if (!GrindEnchantmentsMod.getConfig().disenchant().enabled())
            return false;

        return input1.hasEnchantments() && input2.getItem() == Items.BOOK
            || input2.hasEnchantments() && input1.getItem() == Items.BOOK;
    }

    public static boolean canTakeResult(@SuppressWarnings("unused") ItemStack input1, @SuppressWarnings("unused") ItemStack input2,
                                        IntSupplier cost, PlayerEntity player) {
        return player.getAbilities().creativeMode || player.experienceLevel >= cost.getAsInt();
    }

    public static ItemStack transferEnchantmentsToBook(ItemStack source, boolean allowCurses) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        itemStack.set(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        Object2IntMap<RegistryEntry<Enchantment>> map = GrindEnchantments.getEnchantments(source, allowCurses);

        if (map.isEmpty())
            return ItemStack.EMPTY;

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : map.object2IntEntrySet()) {
            EnchantmentHelper.apply(itemStack, builder -> builder.add(entry.getKey().value(), entry.getIntValue()));
        }

        return itemStack;
    }

    private static ItemStack grind(ItemStack item, boolean allowCurses) {
        ItemStack item2 = item.copy();

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.apply(item2, components ->
            components.remove(enchantment -> allowCurses || !enchantment.value().isCursed()));

        if (item2.isOf(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
            item2 = item2.copyComponentsToNewStack(Items.BOOK, item2.getCount());
        }

        int repairCost = 0;

        for(int j = 0; j < enchantments.getSize(); ++j) {
            repairCost = AnvilScreenHandler.getNextCost(repairCost);
        }

        item2.set(DataComponentTypes.REPAIR_COST, repairCost);
        return item2;
    }
}

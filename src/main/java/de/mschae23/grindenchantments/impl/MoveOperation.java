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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.event.ApplyLevelCostEvent;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoveOperation implements GrindstoneEvents.CanInsert, GrindstoneEvents.UpdateResult, GrindstoneEvents.CanTakeResult, GrindstoneEvents.TakeResult, GrindstoneEvents.LevelCost {
    @Override
    public boolean canInsert(ItemStack stack, ItemStack other, int slotId) {
        return stack.getItem() == Items.BOOK && !other.isOf(Items.BOOK);
    }

    @Override
    public @NotNull ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isMoveOperation(input1, input2)) {
            return ItemStack.EMPTY;
        }

        boolean allowCurses = GrindEnchantmentsMod.getConfig().allowCurses();
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(input1);
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = getFirstEnchantment(enchantments, allowCurses, wrapperLookup);

        if (firstEnchantment == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = input2.copy();
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(result.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
        int targetLevel = builder.getLevel(firstEnchantment.left().value());

        if (targetLevel > 0) {
            if (targetLevel != firstEnchantment.rightInt() || firstEnchantment.rightInt() == firstEnchantment.left().value().getMaxLevel()) {
                return ItemStack.EMPTY;
            } else {
                targetLevel += 1;
            }
        } else {
            targetLevel = firstEnchantment.rightInt();
        }

        builder.add(firstEnchantment.left().value(), targetLevel);

        if (result.getItem() == Items.BOOK) {
            result = result.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
        }

        result.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        return result;
    }

    @Override
    public boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isMoveOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            return canTakeResult(input1, input2, () ->
                GrindEnchantments.getLevelCost(input1, config.move().costFunction(), config.allowCurses(), wrapperLookup), player);
        }

        return true;
    }

    @Override
    public boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!isMoveOperation(input1, input2)) {
            return false;
        }

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
        boolean allowCurses = config.allowCurses();

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(input1);
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = getFirstEnchantment(enchantments, allowCurses, wrapperLookup);

        if (firstEnchantment == null) {
            return false;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
        builder.remove(enchantment -> enchantment.value().equals(firstEnchantment.left().value()));

        ItemStack resultingInput1 = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.set(resultingInput1, builder.build());

        input.setStack(0, resultingInput1);

        if (input2.getItem() == Items.ENCHANTED_BOOK || input2.getCount() == 1)
            input.setStack(1, ItemStack.EMPTY);
        else {
            ItemStack newBookStack = input2.copy();
            newBookStack.setCount(input2.getCount() - 1);
            input.setStack(1, newBookStack);
        }

        if (!player.getAbilities().creativeMode) {
            int cost = GrindEnchantments.getLevelCost(input1, config.move().costFunction(), allowCurses, wrapperLookup);
            ApplyLevelCostEvent.EVENT.invoker().applyLevelCost(cost, player);
        }

        return true;
    }

    @Override
    public int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (isMoveOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            return GrindEnchantments.getLevelCost(input1, config.move().costFunction(), config.allowCurses(), wrapperLookup);
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

    @Nullable
    public static ObjectIntPair<RegistryEntry<Enchantment>> getFirstEnchantment(ItemEnchantmentsComponent enchantments, boolean allowCurses, RegistryWrapper.WrapperLookup wrapperLookup) {
        RegistryEntryList<Enchantment> tooltipOrder = ItemEnchantmentsComponent.getTooltipOrderList(wrapperLookup, RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

        @Nullable
        ObjectIntPair<RegistryEntry<Enchantment>> firstEnchantment = null;

        for (RegistryEntry<Enchantment> entry : tooltipOrder) {
            int level = enchantments.enchantments.getInt(entry);

            if (level > 0 && (allowCurses || !entry.value().isCursed())) {
                firstEnchantment = ObjectIntPair.of(entry, level);
                break;
            }
        }

        byte enchantmentCount = 0;

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentsMap()) {
            if (firstEnchantment == null && !tooltipOrder.contains(entry.getKey()) && (allowCurses || !entry.getKey().value().isCursed())) {
                firstEnchantment = ObjectIntPair.of(entry.getKey(), entry.getIntValue());
            }

            if (allowCurses || !entry.getKey().value().isCursed()) {
                enchantmentCount += 1;
            }

            if (enchantmentCount >= 2) {
                break;
            }
        }

        if (enchantmentCount < 2) {
            return null;
        }

        return firstEnchantment;
    }
}

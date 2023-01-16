package de.martenschaefer.grindenchantments.impl;

import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import de.martenschaefer.grindenchantments.GrindEnchantments;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsV2Config;
import de.martenschaefer.grindenchantments.event.ApplyLevelCostEvent;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents.CanInsert;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents.CanTakeResult;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents.LevelCost;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents.TakeResult;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents.UpdateResult;
import org.jetbrains.annotations.Nullable;

public class DisenchantOperation implements CanInsert, UpdateResult, CanTakeResult, TakeResult, LevelCost {
    @Override
    public boolean canInsert(ItemStack stack, ItemStack other, int slotId) {
        return stack.getItem() == Items.BOOK && !other.isOf(Items.BOOK);
    }

    @Override
    public @Nullable ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (!isDisenchantOperation(input1, input2)) {
            return null;
        }

        ItemStack enchantedItemStack = input1.hasEnchantments() ? input1 : input2;

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
        return transferEnchantmentsToBook(enchantedItemStack, config.allowCurses());
    }

    @Override
    public boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (isDisenchantOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

            boolean stack1Book = input1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? input2 : input1;

            return canTakeResult(input1, input2, () ->
                GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses()), player);
        }

        return true;
    }

    @Override
    public boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input) {
        if (!isDisenchantOperation(input1, input2)) {
            return false;
        }

        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

        boolean stack1Book = input1.getItem() == Items.BOOK;
        ItemStack enchantedItemStack = stack1Book ? input2 : input1;
        ItemStack bookItemStack = stack1Book ? input1 : input2;

        if (!player.getAbilities().creativeMode) {
            int cost = GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses());
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
    public int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player) {
        if (isDisenchantOperation(input1, input2)) {
            GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
            boolean stack1Book = input1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? input2 : input1;

            return GrindEnchantments.getLevelCost(enchantedItemStack, config.disenchant().costFunction(), config.allowCurses());
        }

        return -1;
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

        Map<Enchantment, Integer> map = GrindEnchantments.getEnchantments(source, allowCurses);

        if (map.isEmpty())
            return ItemStack.EMPTY;

        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
        }

        return itemStack;
    }

    private static ItemStack grind(ItemStack item, boolean allowCurses) {
        ItemStack itemStack = item.copy();
        itemStack.removeSubNbt("Enchantments");
        itemStack.removeSubNbt("StoredEnchantments");

        Stream<Map.Entry<Enchantment, Integer>> enchantmentStream = EnchantmentHelper.get(item).entrySet().stream();

        if (allowCurses) // Remove all enchantments
            enchantmentStream = Stream.empty();
        else // Remove all enchantments that are not curses
            enchantmentStream = enchantmentStream.filter(entry -> entry.getKey().isCursed());

        Map<Enchantment, Integer> map = enchantmentStream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        EnchantmentHelper.set(map, itemStack);
        itemStack.setRepairCost(0);

        if (itemStack.getItem() == Items.ENCHANTED_BOOK && map.size() == 0) {
            itemStack = new ItemStack(Items.BOOK);
            if (item.hasCustomName()) {
                itemStack.setCustomName(item.getName());
            }
        }

        for (int i = 0; i < map.size(); ++i) {
            itemStack.setRepairCost(AnvilScreenHandler.getNextCost(itemStack.getRepairCost()));
        }
        return itemStack;
    }
}

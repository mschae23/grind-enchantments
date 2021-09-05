package de.martenschaefer.grindenchantments;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.loader.api.FabricLoader;
import de.martenschaefer.grindenchantments.config.EnchantmentCostConfig;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsConfig;
import fourmisain.taxfreelevels.TaxFreeLevels;

public class GrindEnchantments {
    public static int getLevelCost(ItemStack itemStack1, ItemStack itemStack2) {
        GrindEnchantmentsConfig config = GrindEnchantmentsMod.getConfig();
        ItemStack stack;
        EnchantmentCostConfig costConfig;

        if (Disenchant.isDisenchantOperation(itemStack1, itemStack2)) {
            stack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;
            costConfig = config.disenchant().costConfig();
        } else if (Move.isMoveOperation(itemStack1, itemStack2)) {
            stack = itemStack1;
            costConfig = config.move().costConfig();
        } else {
            return 0;
        }

        return getLevelCost(stack, costConfig);
    }

    public static int getLevelCost(ItemStack stack, EnchantmentCostConfig config) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        double cost = config.countMode().getCost(enchantments.entrySet());

        return (int) (cost * config.costFactor() + config.costOffset());
    }

    public static final class Disenchant {
        private Disenchant() {
        }

        public static boolean isDisenchantOperation(ItemStack itemStack1, ItemStack itemStack2) {
            if (!GrindEnchantmentsMod.getConfig().disenchant().enabled())
                return false;

            return itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
                || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK;
        }

        public static ItemStack doDisenchantOperation(ItemStack itemStack1, ItemStack itemStack2) {
            ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
            result = transferEnchantmentsToBook(result, enchantedItemStack);
            return result;
        }

        public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory input, World world, BlockPos blockPos) {
            boolean stack1Book = itemStack1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? itemStack2 : itemStack1;
            ItemStack bookItemStack = stack1Book ? itemStack1 : itemStack2;

            if (!player.getAbilities().creativeMode) {
                int cost = getLevelCost(enchantedItemStack, bookItemStack);

                if (FabricLoader.getInstance().isModLoaded("taxfreelevels"))
                    player.addExperience(-TaxFreeLevels.getXpDifference(player, 0, cost));
                else
                    player.addExperienceLevels(-cost);
            }
            input.setStack(stack1Book ? 1 : 0, grind(enchantedItemStack));

            if (bookItemStack.getCount() == 1) input.setStack(stack1Book ? 0 : 1, ItemStack.EMPTY);
            else {
                ItemStack bookNew = bookItemStack.copy();
                bookNew.setCount(bookItemStack.getCount() - 1);
                input.setStack(stack1Book ? 0 : 1, bookNew);
            }

            world.syncWorldEvent(1042, blockPos, 0);
        }

        private static ItemStack grind(ItemStack item) {
            ItemStack itemStack = item.copy();
            itemStack.removeSubNbt("Enchantments");
            itemStack.removeSubNbt("StoredEnchantments");

            Map<Enchantment, Integer> map = EnchantmentHelper.get(item).entrySet().stream().filter((entry) ->
                entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

        public static ItemStack transferEnchantmentsToBook(ItemStack target, ItemStack source) {
            ItemStack itemStack = target.copy();
            Map<Enchantment, Integer> map = EnchantmentHelper.get(source).entrySet().stream().filter((entry) ->
                !entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                EnchantedBookItem.addEnchantment(itemStack, new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
            }

            return itemStack;
        }
    }

    public static final class Move {
        private Move() {
        }

        public static boolean isMoveOperation(ItemStack itemStack1, ItemStack itemStack2) {
            if (!GrindEnchantmentsMod.getConfig().move().enabled())
                return false;

            return itemStack1.getItem() == Items.ENCHANTED_BOOK &&
                (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
                    itemStack2.getItem() == Items.BOOK);
        }

        public static ItemStack doMoveOperation(ItemStack itemStack1, ItemStack itemStack2) {
            if (EnchantedBookItem.getEnchantmentNbt(itemStack1).size() < 2) return ItemStack.EMPTY;

            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack1).entrySet().stream().filter((entry) ->
                !entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
            ItemStack result;

            if (itemStack2.getItem() == Items.ENCHANTED_BOOK) {
                Map<Enchantment, Integer> stack2 = EnchantmentHelper.get(itemStack2);
                int level = entry.getValue();

                if (stack2.containsKey(entry.getKey())) {
                    level = stack2.get(entry.getKey());

                    if (level != entry.getValue() || entry.getValue() == entry.getKey().getMaxLevel())
                        return ItemStack.EMPTY;

                    level += 1;
                }

                result = itemStack2.copy();
                EnchantedBookItem.addEnchantment(result, new EnchantmentLevelEntry(entry.getKey(), level));
            } else {
                result = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
            }

            return result;
        }

        public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory input, World world, BlockPos blockPos) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack1);
            Map.Entry<Enchantment, Integer> enchantment = enchantments.entrySet().stream().filter((entry) ->
                !entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().iterator().next();
            enchantments.remove(enchantment.getKey(), enchantment.getValue());

            ItemStack newItemStack1 = new ItemStack(Items.ENCHANTED_BOOK);

            EnchantmentHelper.set(enchantments, newItemStack1);
            input.setStack(0, newItemStack1);

            if (itemStack2.getItem() == Items.ENCHANTED_BOOK || itemStack2.getCount() == 1)
                input.setStack(1, ItemStack.EMPTY);
            else {
                ItemStack newBookItemStack = itemStack2.copy();
                newBookItemStack.setCount(itemStack2.getCount() - 1);
                input.setStack(1, newBookItemStack);
            }

            if (!player.getAbilities().creativeMode) {
                if (FabricLoader.getInstance().isModLoaded("taxfreelevels"))
                    player.addExperience(-TaxFreeLevels.getXpDifference(player, 0,
                        GrindEnchantments.getLevelCost(itemStack1, itemStack2)));
                else
                    player.addExperienceLevels(-GrindEnchantments.getLevelCost(itemStack1, itemStack2));
            }

            world.syncWorldEvent(1042, blockPos, 0);
        }
    }
}

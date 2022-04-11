package de.martenschaefer.grindenchantments;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.fabricmc.loader.api.FabricLoader;
import de.martenschaefer.grindenchantments.config.DedicatedServerConfig;
import de.martenschaefer.grindenchantments.config.EnchantmentCostConfig;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsConfig;
import io.github.fourmisain.taxfreelevels.TaxFreeLevels;
import org.apache.logging.log4j.Level;

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

        return getLevelCost(stack, costConfig, config.allowCurses());
    }

    public static int getLevelCost(ItemStack stack, EnchantmentCostConfig config, boolean allowCurses) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        double cost = config.countMode().getCost(enchantments.entrySet(), allowCurses);

        return (int) (cost * config.costFactor() + config.costOffset());
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack, boolean allowCurses) {
        Stream<Map.Entry<Enchantment, Integer>> enchantments = EnchantmentHelper.get(stack).entrySet().stream();

        if (!allowCurses) // Don't transfer curses if it isn't enabled in the config
            enchantments = enchantments.filter(entry -> !entry.getKey().isCursed());

        return enchantments.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * @param stack mutable; same instance will be returned
     * @return the {@code stack} argument
     */
    public static ItemStack addLevelCostLore(ItemStack stack, IntSupplier cost, boolean canTakeItem, DedicatedServerConfig config) {
        if (!config.alternativeCostDisplay())
            return stack;

        GrindEnchantmentsMod.log(Level.INFO, "Adding level cost lore");

        NbtCompound display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        // TODO Add level cost as separate tag instead of adding it as lore, to make removing it more reliable
        // (will require mixin to ItemStack#getTooltip, which (optionally) has the player as well)
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

        MutableText text = new LiteralText("Enchantment cost: " + cost.getAsInt())
            .formatted(canTakeItem ? Formatting.GREEN : Formatting.RED);

        lore.add(NbtString.of(Text.Serializer.toJson(text)));
        return stack;
    }

    /**
     * @param stack mutable; same instance will be returned
     * @return the {@code stack} argument
     */
    @SuppressWarnings("UnusedReturnValue")
    public static ItemStack removeLevelCostLore(ItemStack stack) {
        // TODO Relies on ItemStacks being mutable AND the stack not being copied into the player inventory before calling this method

        NbtCompound display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        NbtList lore;

        if (display.getType(ItemStack.LORE_KEY) == NbtElement.LIST_TYPE) {
            lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        } else {
            return stack; // The item stack will not have the level cost if Lore is not a list
        }

        for (int i = lore.size() - 1; i >= 0; i--) {
            MutableText loreText = Text.Serializer.fromJson(lore.get(i).asString());

            if (loreText != null && loreText.getString().startsWith("Enchantment cost: ")) {
                lore.remove(i);

                if (lore.size() == 0) { // Remove NBT if there is nothing else in it, so that the item can still stack
                    // The only items that will have this will be enchanted books anyway, which can't stack, but this is cleaner
                    display.remove(ItemStack.LORE_KEY);

                    if (display.getSize() == 0) {
                        stack.removeSubNbt(ItemStack.DISPLAY_KEY);
                    }
                }

                return stack;
            }
        }

        return stack;
    }

    public static boolean canTakeResult(ItemStack itemStack1, ItemStack itemStack2, IntSupplier cost, PlayerEntity player) {
        if (GrindEnchantments.Disenchant.isDisenchantOperation(itemStack1, itemStack2) ||
            GrindEnchantments.Move.isMoveOperation(itemStack1, itemStack2)) {

            return player.getAbilities().creativeMode || player.experienceLevel >= cost.getAsInt();
        }

        return true;
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

        public static ItemStack doDisenchantOperation(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player) {
            ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

            GrindEnchantmentsConfig config = GrindEnchantmentsMod.getConfig();

            IntSupplier levelCost = () -> getLevelCost(enchantedItemStack, config.disenchant().costConfig(), config.allowCurses());

            return addLevelCostLore( // Adds level cost as lore if it is enabled
                transferEnchantmentsToBook(enchantedItemStack, config.allowCurses()),

                // Config for level cost lore
                levelCost, canTakeResult(itemStack1, itemStack2, levelCost, player), config.dedicatedServerConfig()
            );
        }

        public static boolean takeResult(ItemStack itemStack1, ItemStack itemStack2, ItemStack resultStack, PlayerEntity player, Inventory input) {
            GrindEnchantmentsConfig config = GrindEnchantmentsMod.getConfig();

            boolean stack1Book = itemStack1.getItem() == Items.BOOK;
            ItemStack enchantedItemStack = stack1Book ? itemStack2 : itemStack1;
            ItemStack bookItemStack = stack1Book ? itemStack1 : itemStack2;

            if (!player.getAbilities().creativeMode) {
                int cost = getLevelCost(enchantedItemStack, bookItemStack);

                if (FabricLoader.getInstance().isModLoaded("taxfreelevels"))
                    TaxFreeLevels.applyFlattenedXpCost(player, cost);
                else
                    player.addExperienceLevels(-cost);
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

            if (config.dedicatedServerConfig().alternativeCostDisplay()) {
                removeLevelCostLore(resultStack);
            }

            return true;
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

        public static ItemStack transferEnchantmentsToBook(ItemStack source, boolean allowCurses) {
            ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);

            Map<Enchantment, Integer> map = getEnchantments(source, allowCurses);

            if (map.isEmpty())
                return ItemStack.EMPTY;

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

        public static ItemStack doMoveOperation(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player) {
            boolean allowCurses = GrindEnchantmentsMod.getConfig().allowCurses();

            List<Map.Entry<Enchantment, Integer>> firstEnchantments = EnchantmentHelper.get(itemStack1).entrySet().stream()
                .filter(entry -> allowCurses || !entry.getKey().isCursed()).limit(2).toList();

            if (firstEnchantments.size() < 2)
                return ItemStack.EMPTY;

            Map.Entry<Enchantment, Integer> entry = firstEnchantments.get(0);
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

            GrindEnchantmentsConfig config = GrindEnchantmentsMod.getConfig();

            IntSupplier levelCost = () -> getLevelCost(itemStack1, config.move().costConfig(), config.allowCurses());
            return addLevelCostLore(result, levelCost, canTakeResult(itemStack1, itemStack2, levelCost, player), config.dedicatedServerConfig());
        }

        public static boolean takeResult(ItemStack itemStack1, ItemStack itemStack2, ItemStack resultStack, PlayerEntity player, Inventory input) {
            boolean allowCurses = GrindEnchantmentsMod.getConfig().allowCurses();

            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack1);
            Optional<Map.Entry<Enchantment, Integer>> enchantmentOptional = enchantments.entrySet().stream().filter(entry ->
                allowCurses || !entry.getKey().isCursed()).findFirst();

            if (enchantmentOptional.isEmpty())
                return false;

            Map.Entry<Enchantment, Integer> enchantment = enchantmentOptional.get();
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
                    TaxFreeLevels.applyFlattenedXpCost(player, GrindEnchantments.getLevelCost(itemStack1, itemStack2));
                else
                    player.addExperienceLevels(-GrindEnchantments.getLevelCost(itemStack1, itemStack2));
            }

            if (GrindEnchantmentsMod.getConfig().dedicatedServerConfig().alternativeCostDisplay()) {
                removeLevelCostLore(resultStack);
            }

            return true;
        }
    }
}

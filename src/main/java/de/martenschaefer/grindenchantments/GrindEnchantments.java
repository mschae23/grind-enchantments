package de.martenschaefer.grindenchantments;

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
import de.martenschaefer.grindenchantments.config.DedicatedServerConfig;
import de.martenschaefer.grindenchantments.config.EnchantmentCostConfig;
import org.apache.logging.log4j.Level;

public class GrindEnchantments {
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
        // TODO Relies on ItemStacks being mutable AND the stack not being copied into the player inventory before calling this method

        stack.removeSubNbt(GrindEnchantmentsMod.MODID);
        return stack;
    }
}

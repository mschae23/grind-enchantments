package de.martenschaefer.grindenchantments;

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

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class GrindEnchantments {

 private GrindEnchantments() {

 }

 public static int getLevelCost(ItemStack itemStack1, ItemStack itemStack2) {

  if (Extract.isExtractOperation(itemStack1, itemStack2)) {

   return Extract.getLevelCost(itemStack1.hasEnchantments() ? itemStack1 : itemStack2);
  }
  return 0;
 }

 public static class Extract {

  private Extract() {

  }

  public static boolean isExtractOperation(ItemStack itemStack1, ItemStack itemStack2) {

   return itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
           || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK;
  }
  public static ItemStack doExtractOperation(ItemStack itemStack1, ItemStack itemStack2) {

   ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

   ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
   result = transferEnchantmentsToBook(result, enchantedItemStack);
   return result;
  }
  public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory input, World world, BlockPos blockPos) {

   boolean stack1Book = itemStack1.getItem() == Items.BOOK;
   ItemStack enchantedItemStack = stack1Book ? itemStack2 : itemStack1;
   ItemStack bookItemStack = stack1Book ? itemStack1 : itemStack2;

   if (!player.abilities.creativeMode) {
    player.addExperienceLevels(-getLevelCost(enchantedItemStack));
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
  public static int getLevelCost(ItemStack stack) {

   int i = 0;
   Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);

   for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {

    Enchantment enchantment = entry.getKey();
    Integer integer = entry.getValue();
    if (!enchantment.isCursed()) {
     i += integer;
    }
   }

   return i;
  }
  private static ItemStack grind(ItemStack item) {

   ItemStack itemStack = item.copy();
   itemStack.removeSubTag("Enchantments");
   itemStack.removeSubTag("StoredEnchantments");

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
   Map<Enchantment, Integer> map = EnchantmentHelper.get(source);
   Iterator<Map.Entry<Enchantment, Integer>> var5 = map.entrySet().iterator();

   while (true) {
    Map.Entry<Enchantment, Integer> entry;
    Enchantment enchantment;
    int level;
    do {
     if (!var5.hasNext()) {
      return itemStack;
     }

     entry = var5.next();
     enchantment = entry.getKey();
     level = entry.getValue();
    } while (enchantment.isCursed() && EnchantmentHelper.getLevel(enchantment, itemStack) != 0);

    EnchantedBookItem.addEnchantment(itemStack, new EnchantmentLevelEntry(enchantment, level));
   }
  }
 }
}
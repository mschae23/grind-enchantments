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

import java.util.Map;
import java.util.stream.Collectors;

public class GrindEnchantments {

 public static int getLevelCost(ItemStack itemStack1, ItemStack itemStack2) {

  if (Extract.isExtractOperation(itemStack1, itemStack2)) {

   return Extract.getLevelCost(itemStack1.hasEnchantments() ? itemStack1 : itemStack2);
  }
  else if (Transfer.isTransferOperation(itemStack1, itemStack2)) {

   return Transfer.getLevelCost(itemStack1);
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
   Map<Enchantment, Integer> map = EnchantmentHelper.get(source).entrySet().stream().filter((entry) ->
    !entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

   for(Map.Entry<Enchantment, Integer> entry: map.entrySet()) {

    EnchantedBookItem.addEnchantment(itemStack, new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
   }
   return itemStack;
  }
 }
 public static class Transfer {

  public static boolean isTransferOperation(ItemStack itemStack1, ItemStack itemStack2) {

   return itemStack1.getItem() == Items.ENCHANTED_BOOK &&
           (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
                   itemStack2.getItem() == Items.BOOK);
  }
  public static ItemStack doTransferOperation(ItemStack itemStack1, ItemStack itemStack2) {

   if(EnchantedBookItem.getEnchantmentTag(itemStack1).size() < 2) return null;

   Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack1).entrySet().stream().filter((entry) ->
    !entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
   Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
   ItemStack result;

   if(itemStack2.getItem() == Items.ENCHANTED_BOOK) {

    result = itemStack2.copy();
    EnchantedBookItem.addEnchantment(result, new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
   }
   else {

    result = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
   }
   return result;
  }
  public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory input, World world, BlockPos blockPos) {

   Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack1);
   Map.Entry<Enchantment, Integer> enchantment = enchantments.entrySet().stream().filter((entry) ->
    !(entry.getKey().isCursed() && EnchantmentHelper.getLevel(entry.getKey(), itemStack1) != 0))
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().iterator().next();
   enchantments.remove(enchantment.getKey(), enchantment.getValue());

   ItemStack newItemStack1 = new ItemStack(Items.ENCHANTED_BOOK);

   EnchantmentHelper.set(enchantments, newItemStack1);
   input.setStack(0, newItemStack1);

   if(itemStack2.getItem() == Items.ENCHANTED_BOOK || itemStack2.getCount() == 1)
    input.setStack(1, ItemStack.EMPTY);

   else {

    ItemStack newBookItemStack = itemStack2.copy();
    newBookItemStack.setCount(itemStack2.getCount() - 1);
    input.setStack(1,	newBookItemStack);
   }

   if (!player.abilities.creativeMode) {
    player.addExperienceLevels(-GrindEnchantments.getLevelCost(itemStack1, itemStack2));
   }
   world.syncWorldEvent(1042, blockPos, 0);
  }
  public static int getLevelCost(ItemStack itemStack1) {

   int i = 0;
   Map<Enchantment, Integer> map = EnchantmentHelper.get(itemStack1);

   for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {

    Enchantment enchantment = entry.getKey();
    if (!enchantment.isCursed()) i++;
   }

   return (int) (i / 2.0 + 0.5);
  }
 }
}
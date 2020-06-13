package de.martenschaefer.grindenchantments;

import net.minecraft.container.AnvilContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class GrindEnchantments {

 public static int getLevelCost(ItemStack itemStack1, ItemStack itemStack2) {

  if (Extract.isExtractOperation(itemStack1, itemStack2)) {

   ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;
   return Extract.getLevelCost(enchantedItemStack);
  }
  else if(Transfer.isTransferOperation(itemStack1, itemStack2)) {

   return Transfer.getLevelCost(itemStack1);
  }
  return 0;
 }
 public static class Extract {

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
  public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory craftingInventory, World world, BlockPos blockPos) {

   boolean stack1Book = itemStack1.getItem() == Items.BOOK;
   ItemStack enchantedItemStack = stack1Book? itemStack2 : itemStack1;
   ItemStack bookItemStack = stack1Book? itemStack1 : itemStack2;

   if (!player.abilities.creativeMode) {
    player.addExperienceLevels(-getLevelCost(craftingInventory.getInvStack(0)));
   }
   craftingInventory.setInvStack(stack1Book? 1 : 0, grind(enchantedItemStack));

   if(bookItemStack.getCount() == 1)
    craftingInventory.setInvStack(stack1Book? 0 : 1, ItemStack.EMPTY);
   else {

    ItemStack newBookItemStack = bookItemStack.copy();
    newBookItemStack.setCount(bookItemStack.getCount() - 1);
    craftingInventory.setInvStack(stack1Book? 0 : 1,	newBookItemStack);
   }
   world.playLevelEvent(1042, blockPos, 0);
  }
  public static int getLevelCost(ItemStack stack) {

   int i = 0;
   Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);

   for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
    Enchantment enchantment = entry.getKey();
    Integer integer = entry.getValue();
    if (!enchantment.isCursed()) {
     i += integer;
    }
   }

   return i;
  }
  public static ItemStack transferEnchantmentsToBook(ItemStack target, ItemStack source) {

   ItemStack itemStack = target.copy();
   Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(source);
   Iterator<Map.Entry<Enchantment, Integer>> var5 = map.entrySet().iterator();

   while(true) {
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
    } while(enchantment.isCursed() && EnchantmentHelper.getLevel(enchantment, itemStack) != 0);

    EnchantedBookItem.addEnchantment(itemStack, new InfoEnchantment(enchantment, level));
   }
  }
  public static ItemStack grind(ItemStack item) {

   ItemStack itemStack = item.copy();
   itemStack.removeSubTag("Enchantments");
   itemStack.removeSubTag("StoredEnchantments");

   Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(item).entrySet().stream().filter((entry) -> entry.getKey().isCursed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
   EnchantmentHelper.set(map, itemStack);
   itemStack.setRepairCost(0);
   if (itemStack.getItem() == Items.ENCHANTED_BOOK && map.size() == 0) {
    itemStack = new ItemStack(Items.BOOK);
    if (item.hasCustomName()) {
     itemStack.setCustomName(item.getName());
    }
   }

   for(int i = 0; i < map.size(); ++i) {
    itemStack.setRepairCost(AnvilContainer.getNextCost(itemStack.getRepairCost()));
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

   Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack1);
   Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
   ItemStack result;

   if(itemStack2.getItem() == Items.ENCHANTED_BOOK) {

    result = itemStack2.copy();
    EnchantedBookItem.addEnchantment(result, new InfoEnchantment(entry.getKey(), entry.getValue()));
   }
   else {

    result = EnchantedBookItem.forEnchantment(new InfoEnchantment(entry.getKey(), entry.getValue()));
   }
   return result;
  }
  public static void takeResult(ItemStack itemStack1, ItemStack itemStack2, PlayerEntity player, Inventory craftingInventory, World world, BlockPos blockPos) {

   Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack1);
   Map.Entry<Enchantment, Integer> enchantment = enchantments.entrySet().iterator().next();
   enchantments.remove(enchantment.getKey(), enchantment.getValue());

   ItemStack newItemStack1 = new ItemStack(Items.ENCHANTED_BOOK);

   EnchantmentHelper.set(enchantments, newItemStack1);
   craftingInventory.setInvStack(0, newItemStack1);

   if(itemStack2.getItem() == Items.ENCHANTED_BOOK || itemStack2.getCount() == 1)
    craftingInventory.setInvStack(1, ItemStack.EMPTY);
   else {

    ItemStack newBookItemStack = itemStack2.copy();
    newBookItemStack.setCount(itemStack2.getCount() - 1);
    craftingInventory.setInvStack(1,	newBookItemStack);
   }

   if (!player.abilities.creativeMode) {
    player.addExperienceLevels(-getLevelCost(itemStack1));
   }
   world.playLevelEvent(1042, blockPos, 0);
  }
  public static int getLevelCost(ItemStack stack) {

   int i = 0;
   Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);

   for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
    Enchantment enchantment = entry.getKey();
    if (!enchantment.isCursed()) i++;
   }

   return (int) (i / 2.0 + 0.5);
  }
 }

}
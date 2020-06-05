package de.martenschaefer.grindenchantments;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Iterator;
import java.util.Map;

public class LevelCostHelper {

 private LevelCostHelper() {}

 @Environment(EnvType.CLIENT)
 public static int getLevelCost(ItemStack itemStack1, ItemStack itemStack2) {

  if (itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
          || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK) {

   ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;
   return getLevelCost(enchantedItemStack);
  }
  else if(itemStack1.getItem() == Items.ENCHANTED_BOOK &&
          (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
                  itemStack2.getItem() == Items.BOOK)) {

   return getTransferLevelCost(itemStack1);
  }
  return 0;
 }
 private static int getLevelCost(ItemStack stack) {

  int i = 0;
  Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
  Iterator<Map.Entry<Enchantment, Integer>> var4 = map.entrySet().iterator();

  while (var4.hasNext()) {
   Map.Entry<Enchantment, Integer> entry = var4.next();
   Enchantment enchantment = (Enchantment) entry.getKey();
   Integer integer = entry.getValue();
   if (!enchantment.isCursed()) {
    i += integer;
   }
  }

  return i;
 }
 private static int getTransferLevelCost(ItemStack stack) {

  int i = 0;
  Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
  Iterator<Map.Entry<Enchantment, Integer>> var4 = map.entrySet().iterator();

  while (var4.hasNext()) {
   Map.Entry<Enchantment, Integer> entry = var4.next();
   Enchantment enchantment = (Enchantment) entry.getKey();
   if (!enchantment.isCursed()) i++;
  }

  return (int) (i / 2.0 + 0.5);
 }
}
package de.martenschaefer.grindenchantments.mixin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.martenschaefer.grindenchantments.LevelCostHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.container.Slot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@Mixin(GrindstoneContainer.class)
public abstract class GrindstoneContainerMixin extends Container {

 protected GrindstoneContainerMixin(ContainerType<?> type, int syncId) {

  super(type, syncId);
 }

 @Shadow
 private Inventory craftingInventory;
 @Shadow
 private Inventory resultInventory;
 @Shadow
 private BlockContext context;
 private ItemStack transferEnchantmentsToBook(ItemStack target, ItemStack source) {

  ItemStack itemStack = target.copy();
  Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(source);
  Iterator<Entry<Enchantment, Integer>> var5 = map.entrySet().iterator();

  while(true) {
   Entry<Enchantment, Integer> entry;
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
 private ItemStack grind(ItemStack item) {

  ItemStack itemStack = item.copy();
  itemStack.removeSubTag("Enchantments");
  itemStack.removeSubTag("StoredEnchantments");

  Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(item).entrySet().stream().filter((entry) -> {
   return ((Enchantment)entry.getKey()).isCursed();
  }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

 @Inject(at = @At("HEAD"), method = "updateResult", cancellable = true)
 private void onUpdateResult(CallbackInfo ci) {

  ItemStack itemStack1 = this.craftingInventory.getInvStack(0);
  ItemStack itemStack2 = this.craftingInventory.getInvStack(1);

  if (itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
          || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK) {

   ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

   ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
   result = transferEnchantmentsToBook(result, enchantedItemStack);
   this.resultInventory.setInvStack(0, result);
   this.sendContentUpdates();
   ci.cancel();
  }
  else if(itemStack1.getItem() == Items.ENCHANTED_BOOK &&
          (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
           itemStack2.getItem() == Items.BOOK)) {

   if(EnchantedBookItem.getEnchantmentTag(itemStack1).size() < 2) return;

   Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack1);
   Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
   ItemStack result;

   if(itemStack2.getItem() == Items.ENCHANTED_BOOK) {

    result = itemStack2.copy();
    EnchantedBookItem.addEnchantment(result, new InfoEnchantment(entry.getKey(), entry.getValue()));
   }
   else {

    result = EnchantedBookItem.forEnchantment(new InfoEnchantment(entry.getKey(), entry.getValue()));
   }
   this.resultInventory.setInvStack(0, result);
   this.sendContentUpdates();
   ci.cancel();
  }
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/GrindstoneContainer;addSlot(Lnet/minecraft/container/Slot;)Lnet/minecraft/container/Slot;", ordinal = 0), index = 0)
 public Slot modifySlot0(Slot slot) {

  return new Slot(this.craftingInventory, 0, 49, 19) {

   public boolean canInsert(ItemStack stack) {

    return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK
            || stack.hasEnchantments();
   }
  };
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/GrindstoneContainer;addSlot(Lnet/minecraft/container/Slot;)Lnet/minecraft/container/Slot;", ordinal = 1), index = 0)
 public Slot modifySlot1(Slot slot) {

  return new Slot(this.craftingInventory, 1, 49, 40) {

   public boolean canInsert(ItemStack stack) {

    return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK
            || stack.hasEnchantments();
   }
  };
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/GrindstoneContainer;addSlot(Lnet/minecraft/container/Slot;)Lnet/minecraft/container/Slot;", ordinal = 2), index = 0)
 public Slot modifyResultSlot(Slot slot) {

  return new Slot(this.resultInventory, 2, 129, 34) {

   public boolean canInsert(ItemStack stack) {

    return false;
   }
   public boolean canTakeItems(PlayerEntity playerEntity) {

    ItemStack itemStack1 = craftingInventory.getInvStack(0);
    ItemStack itemStack2 = craftingInventory.getInvStack(1);

    if (itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
            || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK) {

     ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

     return (playerEntity.abilities.creativeMode || playerEntity.experienceLevel >= LevelCostHelper.getLevelCost(craftingInventory.getInvStack(0), craftingInventory.getInvStack(1)));
    }
    else if(itemStack1.getItem() == Items.ENCHANTED_BOOK &&
            (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
                    itemStack2.getItem() == Items.BOOK)) {

     return (playerEntity.abilities.creativeMode || playerEntity.experienceLevel >= LevelCostHelper.getLevelCost(craftingInventory.getInvStack(0), craftingInventory.getInvStack(1)));
    }
    return true;
   }
   public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {

    context.run((world, blockPos) -> {

     ItemStack itemStack1 = craftingInventory.getInvStack(0);
     ItemStack itemStack2 = craftingInventory.getInvStack(1);

     if (itemStack1.hasEnchantments() && itemStack2.getItem() == Items.BOOK
             || itemStack2.hasEnchantments() && itemStack1.getItem() == Items.BOOK) {

      boolean stack1Book = itemStack1.getItem() == Items.BOOK;
      ItemStack enchantedItemStack = stack1Book? itemStack2 : itemStack1;
      ItemStack bookItemStack = stack1Book? itemStack1 : itemStack2;

      if (!player.abilities.creativeMode) {
       player.addExperienceLevels(-LevelCostHelper.getLevelCost(craftingInventory.getInvStack(0), craftingInventory.getInvStack(1)));
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
      return;
     }
     else if(itemStack1.getItem() == Items.ENCHANTED_BOOK &&
             (itemStack2.getItem() == Items.ENCHANTED_BOOK ||
                     itemStack2.getItem() == Items.BOOK)) {

      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack1);
      Entry<Enchantment, Integer> enchantment = enchantments.entrySet().iterator().next();
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
       player.addExperienceLevels(-LevelCostHelper.getLevelCost(itemStack1, itemStack2));
      }
      world.playLevelEvent(1042, blockPos, 0);
      return;
     }

     int i = this.getExperience(world);

     while (i > 0) {
      int j = ExperienceOrbEntity.roundToOrbSize(i);
      i -= j;
      world.spawnEntity(new ExperienceOrbEntity(world, (double) blockPos.getX(), (double) blockPos.getY() + 0.5D,
              (double) blockPos.getZ() + 0.5D, j));
     }
     craftingInventory.setInvStack(0, ItemStack.EMPTY);
     craftingInventory.setInvStack(1, ItemStack.EMPTY);

     world.playLevelEvent(1042, blockPos, 0);
    });

    return stack;
   }

   private int getExperience(World world) {

    int ix = 0;
    int i = ix + this.getExperience(craftingInventory.getInvStack(0));
    i += this.getExperience(craftingInventory.getInvStack(1));
    if (i > 0) {
     int j = (int) Math.ceil((double) i / 2.0D);
     return j + world.random.nextInt(j);
    } else {
     return 0;
    }
   }

   private int getExperience(ItemStack stack) {

    int i = 0;
    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
    Iterator<Entry<Enchantment, Integer>> var4 = map.entrySet().iterator();

    while (var4.hasNext()) {
     Entry<Enchantment, Integer> entry = var4.next();
     Enchantment enchantment = (Enchantment) entry.getKey();
     Integer integer = (Integer) entry.getValue();
     if (!enchantment.isCursed()) {
      i += enchantment.getMinimumPower(integer);
     }
    }

    return i;
   }
  };
 }
}

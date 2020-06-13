package de.martenschaefer.grindenchantments.mixin;

import de.martenschaefer.grindenchantments.GrindEnchantments;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.container.Slot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Mixin(GrindstoneContainer.class)
public abstract class GrindstoneContainerMixin extends Container {

 @Shadow
 private Inventory craftingInventory;
 @Shadow
 private Inventory resultInventory;
 @Shadow
 private BlockContext context;

 protected GrindstoneContainerMixin(ContainerType<?> type, int syncId) {

  super(type, syncId);
 }
 @Inject(at = @At("HEAD"), method = "updateResult", cancellable = true)
 private void onUpdateResult(CallbackInfo ci) {

  ItemStack itemStack1 = this.craftingInventory.getInvStack(0);
  ItemStack itemStack2 = this.craftingInventory.getInvStack(1);

  if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

   this.resultInventory.setInvStack(0, GrindEnchantments.Extract.doExtractOperation(itemStack1, itemStack2));
   this.sendContentUpdates();
   ci.cancel();
  }
  else if(GrindEnchantments.Transfer.isTransferOperation(itemStack1, itemStack2)) {

   ItemStack result = GrindEnchantments.Transfer.doTransferOperation(itemStack1, itemStack2);

   if(result == null) return;

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

    if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

     ItemStack enchantedItemStack = itemStack1.hasEnchantments() ? itemStack1 : itemStack2;

     return (playerEntity.abilities.creativeMode || playerEntity.experienceLevel >= GrindEnchantments.Extract.getLevelCost(enchantedItemStack));
    }
    else if(GrindEnchantments.Transfer.isTransferOperation(itemStack1, itemStack2)) {

     return (playerEntity.abilities.creativeMode || playerEntity.experienceLevel >= GrindEnchantments.Transfer.getLevelCost(craftingInventory.getInvStack(0)));
    }
    return true;
   }
   public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {

    context.run((world, blockPos) -> {

     ItemStack itemStack1 = craftingInventory.getInvStack(0);
     ItemStack itemStack2 = craftingInventory.getInvStack(1);

     if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

      GrindEnchantments.Extract.takeResult(itemStack1, itemStack2, player, craftingInventory, world, blockPos);
      return;
     }
     else if(GrindEnchantments.Transfer.isTransferOperation(itemStack1, itemStack2)) {

      GrindEnchantments.Transfer.takeResult(itemStack1, itemStack2, player, craftingInventory, world, blockPos);
      return;
     }

     // Vanilla Grindstone take item logic

     int i = this.getExperience(world);

     while (i > 0) {
      int j = ExperienceOrbEntity.roundToOrbSize(i);
      i -= j;
      world.spawnEntity(new ExperienceOrbEntity(world, blockPos.getX(), (double) blockPos.getY() + 0.5D,
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

    for (Entry<Enchantment, Integer> entry : map.entrySet()) {
     Enchantment enchantment = entry.getKey();
     Integer integer = entry.getValue();
     if (!enchantment.isCursed()) {
      i += enchantment.getMinimumPower(integer);
     }
    }

    return i;
   }
  };
 }
}

package de.martenschaefer.grindenchantments.mixin;

import de.martenschaefer.grindenchantments.GrindEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
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

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {

 protected GrindstoneScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {

  super(type, syncId);
 }
 @Shadow
 private Inventory input;
 @Shadow
 private Inventory result;
 @Shadow
 private ScreenHandlerContext context;

 @Inject(at = @At("HEAD"), method = "updateResult", cancellable = true)
 private void onUpdateResult(CallbackInfo ci) {

  ItemStack itemStack1 = this.input.getStack(0);
  ItemStack itemStack2 = this.input.getStack(1);

  if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

   this.result.setStack(0, GrindEnchantments.Extract.doExtractOperation(itemStack1, itemStack2));
   this.sendContentUpdates();
   ci.cancel();
  }
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 0), index = 0)
 public Slot modifySlot0(Slot slot) {

  return new Slot(this.input, 0, 49, 19) {

   @Override
   public boolean canInsert(ItemStack stack) {

    return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || (stack.getItem() == Items.BOOK && input.getStack(1).getItem() != Items.BOOK)
            || stack.hasEnchantments();
   }
  };
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 1), index = 0)
 public Slot modifySlot1(Slot slot) {

  return new Slot(this.input, 1, 49, 40) {

   @Override
   public boolean canInsert(ItemStack stack) {

    return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || (stack.getItem() == Items.BOOK && input.getStack(0).getItem() != Items.BOOK)
            || stack.hasEnchantments();
   }
  };
 }
 @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 2), index = 0)
 public Slot modifyResultSlot(Slot slot) {

  return new Slot(this.result, 2, 129, 34) {

   public boolean canInsert(ItemStack stack) {

    return false;
   }
   public boolean canTakeItems(PlayerEntity playerEntity) {

    ItemStack itemStack1 = input.getStack(0);
    ItemStack itemStack2 = input.getStack(1);

    if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

     return (playerEntity.abilities.creativeMode || playerEntity.experienceLevel >=
      GrindEnchantments.getLevelCost(itemStack1, itemStack2));
    }
    return true;
   }
   public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {

    context.run((world, blockPos) -> {

     ItemStack itemStack1 = input.getStack(0);
     ItemStack itemStack2 = input.getStack(1);

     if (GrindEnchantments.Extract.isExtractOperation(itemStack1, itemStack2)) {

      GrindEnchantments.Extract.takeResult(itemStack1, itemStack2, player, input, world, blockPos);
      return;
     }

     int i = this.getExperience(world);

     while (i > 0) {
      int j = ExperienceOrbEntity.roundToOrbSize(i);
      i -= j;
      world.spawnEntity(new ExperienceOrbEntity(world, (double) blockPos.getX(), (double) blockPos.getY() + 0.5D,
              (double) blockPos.getZ() + 0.5D, j));
     }
     input.setStack(0, ItemStack.EMPTY);
     input.setStack(1, ItemStack.EMPTY);

     world.syncWorldEvent(1042, blockPos, 0);
    });

    return stack;
   }

   private int getExperience(World world) {

    int ix = 0;
    int i = ix + this.getExperience(input.getStack(0));
    i += this.getExperience(input.getStack(1));
    if (i > 0) {
     int j = (int) Math.ceil((double) i / 2.0D);
     return j + world.random.nextInt(j);
    } else {
     return 0;
    }
   }

   private int getExperience(ItemStack stack) {

    int i = 0;
    Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
    Iterator<Entry<Enchantment, Integer>> var4 = map.entrySet().iterator();

    while (var4.hasNext()) {
     Entry<Enchantment, Integer> entry = var4.next();
     Enchantment enchantment = (Enchantment) entry.getKey();
     Integer integer = (Integer) entry.getValue();
     if (!enchantment.isCursed()) {
      i += enchantment.getMinPower(integer);
     }
    }

    return i;
   }

  };
 }
}
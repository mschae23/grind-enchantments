package de.martenschaefer.grindenchantments.mixin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.container.Slot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

@Mixin(GrindstoneScreen.class)
public abstract class GrindstoneScreenMixin extends ContainerScreen<GrindstoneContainer> {
 
	public GrindstoneScreenMixin(GrindstoneContainer container, PlayerInventory playerInventory, Text name) {

		super(container, playerInventory, name);
	}
	@Inject(method = "drawForeground", at = @At("RETURN"))
	protected void onDrawForeground(int mouseX, int mouseY, CallbackInfo ci) {
		
		int i = getLevelCost(((GrindstoneScreen)(Object) this).getContainer().getSlot(0), ((GrindstoneScreen)(Object) this).getContainer().getSlot(1));
  if (i > 0) {
     int j = 8453920;
     boolean bl = true;
     String string = I18n.translate("container.repair.cost", i);
     if (!(((GrindstoneScreen)(Object) this).getContainer()).getSlot(2).hasStack()) {
     	
        bl = false;
     } else if (!(((GrindstoneScreen)(Object) this).getContainer()).getSlot(2).canTakeItems(playerInventory.player)) {
        j = 16736352;
     }

     if (bl) {
        int k = this.containerWidth - 8 - this.font.getStringWidth(string) - 2;
        fill(k - 2, 67, this.containerWidth - 8, 79, 1325400064);
        this.font.drawWithShadow(string, (float)k, 69.0F, j);
     }
  }
	}
 private int getLevelCost(Slot slot0, Slot slot1) {
  
		ItemStack itemStack = slot0.getStack();
		ItemStack itemStack2 = slot1.getStack();

		if (itemStack.hasEnchantments() && itemStack2.getItem() == Items.BOOK
				|| itemStack2.hasEnchantments() && itemStack.getItem() == Items.BOOK) {

			ItemStack enchantedItemStack = itemStack.hasEnchantments() ? itemStack : itemStack2;
			return getLevelCost(enchantedItemStack);
		}
		return 0;
 }
	private int getLevelCost(ItemStack stack) {
				
				int i = 0;
				Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
				Iterator<Entry<Enchantment, Integer>> var4 = map.entrySet().iterator();

				while (var4.hasNext()) {
					Entry<Enchantment, Integer> entry = var4.next();
					Enchantment enchantment = (Enchantment) entry.getKey();
					Integer integer = entry.getValue();
					if (!enchantment.isCursed()) {
						i += integer;
					}
				}

				return i;
	}
}
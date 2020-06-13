package de.martenschaefer.grindenchantments.mixin;

import de.martenschaefer.grindenchantments.GrindEnchantments;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreen.class)
public abstract class GrindstoneScreenMixin extends ContainerScreen<GrindstoneContainer> {
 
	public GrindstoneScreenMixin(GrindstoneContainer container, PlayerInventory playerInventory, Text name) {

		super(container, playerInventory, name);
	}
	@Inject(method = "drawForeground", at = @At("RETURN"))
	protected void onDrawForeground(int mouseX, int mouseY, CallbackInfo ci) {
		
		int i = GrindEnchantments.getLevelCost(((GrindstoneScreen)(Object) this).getContainer().getSlot(0).getStack(),
										((GrindstoneScreen)(Object) this).getContainer().getSlot(1).getStack());
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
}
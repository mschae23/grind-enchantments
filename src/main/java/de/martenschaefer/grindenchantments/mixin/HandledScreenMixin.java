package de.martenschaefer.grindenchantments.mixin;

import java.util.Objects;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import de.martenschaefer.grindenchantments.GrindEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow
    protected int backgroundWidth = 176;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    protected void onDrawForeground(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        if ((Object) this instanceof GrindstoneScreen screen) {
            grindenchantments_drawForeground(matrices, mouseX, mouseY, screen);
        }
    }

    public void grindenchantments_drawForeground(MatrixStack matrices, int mouseX, int mouseY, GrindstoneScreen screen) {
        int i = GrindEnchantments.getLevelCost(screen.getScreenHandler().getSlot(0).getStack(), screen.getScreenHandler().getSlot(1).getStack());

        if (i > 0) {
            int j = 8453920;
            boolean bl = true;
            String string = I18n.translate("container.repair.cost", i);
            if (!screen.getScreenHandler().getSlot(2).hasStack()) {
                bl = false;
            } else {
                if (!screen.getScreenHandler().getSlot(2).canTakeItems(Objects.requireNonNull(this.client).player)) {
                    j = 16736352;
                }
            }

            if (bl) {
                int k = this.backgroundWidth - 8 - this.textRenderer.getWidth(string) - 2;
                fill(matrices, k - 2, 67, this.backgroundWidth - 8, 79, 1325400064);
                this.textRenderer.drawWithShadow(matrices, string, (float) k, 69.0F, j);
            }
        }
    }
}

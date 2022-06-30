package de.martenschaefer.grindenchantments.mixin;

import java.util.Objects;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.text.Text;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GrindstoneScreen.class)
public abstract class GrindstoneScreenMixin extends HandledScreen<GrindstoneScreenHandler> {
    public GrindstoneScreenMixin(GrindstoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);

        if (!GrindEnchantmentsMod.getConfig().showCost())
            return; // Don't show the enchantment cost

        int cost = GrindstoneEvents.LEVEL_COST.invoker().getLevelCost(this.handler.getSlot(0).getStack(), this.handler.getSlot(1).getStack(),
            Objects.requireNonNull(this.client).player);

        if (cost > 0) {
            int j = 8453920;
            boolean bl = true;
            String string = I18n.translate("container.repair.cost", cost);
            if (!this.handler.getSlot(2).hasStack()) {
                bl = false;
            } else {
                if (!this.handler.getSlot(2).canTakeItems(Objects.requireNonNull(this.client).player)) {
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

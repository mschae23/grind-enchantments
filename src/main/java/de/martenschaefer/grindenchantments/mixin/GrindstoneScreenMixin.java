package de.martenschaefer.grindenchantments.mixin;

import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {super.drawForeground(context, mouseX, mouseY);
        if (!GrindEnchantmentsMod.getConfig().clientConfig().showLevelCost())
            return; // Don't show the enchantment cost

        int cost = GrindstoneEvents.LEVEL_COST.invoker().getLevelCost(this.handler.getSlot(0).getStack(), this.handler.getSlot(1).getStack(),
            Objects.requireNonNull(this.client).player);

        if (cost > 0) {
            int j = 8453920;
            Text text;

            if (!this.handler.getSlot(2).hasStack()) {
                text = null;
            } else {
                text = Text.translatable("container.repair.cost", cost);
                if (!this.handler.getSlot(2).canTakeItems(this.client.player)) {
                    j = 16736352;
                }
            }

            if (text != null) {
                int k = this.backgroundWidth - 8 - this.textRenderer.getWidth(text) - 2;
                context.fill(k - 2, 67, this.backgroundWidth - 8, 79, 1325400064);
                context.drawTextWithShadow(this.textRenderer, text, k, 69, j);
            }
        }
    }
}

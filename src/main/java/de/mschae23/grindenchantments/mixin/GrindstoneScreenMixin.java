/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Grind enchantments.
 *
 * Grind enchantments is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.grindenchantments.mixin;

import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.text.Text;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GrindstoneScreen.class)
public abstract class GrindstoneScreenMixin extends HandledScreen<GrindstoneScreenHandler> {
    public GrindstoneScreenMixin(GrindstoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {super.drawForeground(context, mouseX, mouseY);
        if (!GrindEnchantmentsMod.getClientConfig().showLevelCost())
            return; // Don't show the enchantment cost

        MinecraftClient client = Objects.requireNonNull(this.client);
        ClientPlayerEntity player = Objects.requireNonNull(client.player);
        ClientWorld world = Objects.requireNonNull(client.world);
        DynamicRegistryManager registryManager = world.getRegistryManager();

        int cost = GrindstoneEvents.LEVEL_COST.invoker().getLevelCost(this.handler.getSlot(0).getStack(), this.handler.getSlot(1).getStack(),
            player, registryManager);

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

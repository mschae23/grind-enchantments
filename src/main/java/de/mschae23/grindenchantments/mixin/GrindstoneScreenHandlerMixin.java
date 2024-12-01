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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.WorldEvents;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.event.GrindstoneEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    @Shadow
    @Final
    public Inventory input;
    @Final
    @Shadow
    public Inventory result;

    @Unique
    private PlayerEntity grindenchantments_player;

    protected GrindstoneScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(at = @At("RETURN"), method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
    private void onReturnConstructor(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context, CallbackInfo ci) {
        this.grindenchantments_player = playerInventory.player;
    }

    @Inject(at = @At("RETURN"), method = "updateResult", cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        ItemStack input1 = this.input.getStack(0);
        ItemStack input2 = this.input.getStack(1);

        // PlayerEntity player = ((PlayerInventory) this.slots.get(3).inventory).player;
        PlayerEntity player = this.grindenchantments_player;

        @Nullable
        ItemStack result = GrindstoneEvents.UPDATE_RESULT.invoker().onUpdateResult(input1, input2, player);

        if (result != null) {
            int cost = GrindstoneEvents.LEVEL_COST.invoker().getLevelCost(input1, input2, player);

            if (cost >= 0) {
                boolean canTake = GrindstoneEvents.CAN_TAKE_RESULT.invoker().canTakeResult(input1, input2, player);

                result = GrindEnchantments.addLevelCostNbt(result, () -> cost, canTake, GrindEnchantmentsMod.getConfig().dedicatedServerConfig());
            }

            this.result.setStack(0, result);
            this.sendContentUpdates();
            ci.cancel();
        }
    }

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onInsertResultItem(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack1, Slot slot, ItemStack itemStack2) {
        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

        if (config.dedicatedServerConfig().alternativeCostDisplay()) {
            GrindEnchantments.removeLevelCostNbt(itemStack2);
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$2")
    public static class Anonymous2Mixin extends Slot {
        @Shadow
        @Final
        GrindstoneScreenHandler field_16777;

        public Anonymous2Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
        private void canInsertBooks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Inventory input = this.field_16777.input;

            cir.setReturnValue(cir.getReturnValueZ() || GrindstoneEvents.CAN_INSERT.invoker().canInsert(stack, input.getStack(1), 0));
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$3")
    public static class Anonymous3Mixin extends Slot {
        @Shadow
        @Final
        GrindstoneScreenHandler field_16778;

        public Anonymous3Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
        private void canInsertBooks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Inventory input = this.field_16778.input;

            cir.setReturnValue(cir.getReturnValueZ() || GrindstoneEvents.CAN_INSERT.invoker().canInsert(stack, input.getStack(0), 1));
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
    public static abstract class Anonymous4Mixin extends Slot {
        @Final
        @Shadow
        ScreenHandlerContext field_16779;
        @Shadow
        @Final
        GrindstoneScreenHandler field_16780;

        public Anonymous4Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
        private void onTakeResult(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
            Inventory input = this.field_16780.input;

            ItemStack input1 = input.getStack(0);
            ItemStack input2 = input.getStack(1);

            boolean success = GrindstoneEvents.TAKE_RESULT.invoker().onTakeResult(input1, input2, stack, player, input);

            if (GrindEnchantmentsMod.getConfig().dedicatedServerConfig().alternativeCostDisplay()) {
                GrindEnchantments.removeLevelCostNbt(stack);
            }

            if (success) {
                this.field_16779.run((world, pos) -> world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, pos, 0)); // Plays grindstone sound
                ci.cancel();
            }
        }

        /**
         * @author mschae23
         */
        @Override
        public boolean canTakeItems(PlayerEntity player) {
            Inventory input = this.field_16780.input;

            ItemStack input1 = input.getStack(0);
            ItemStack input2 = input.getStack(1);

            return GrindstoneEvents.CAN_TAKE_RESULT.invoker().canTakeResult(input1, input2, player);
        }
    }
}

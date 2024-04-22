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

import java.util.function.Supplier;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;
import de.mschae23.grindenchantments.item.GrindEnchantmentsDataComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @WrapOperation(method = "sendContentUpdates", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;", remap = false))
    private com.google.common.base.Supplier<ItemStack> addLoreOnSendUpdates(com.google.common.base.Supplier<ItemStack> delegate, Operation<com.google.common.base.Supplier<ItemStack>> original) {
        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

        if (!config.dedicatedServerConfig().alternativeCostDisplay()) {
            return original.call(delegate);
        }

        return original.call((com.google.common.base.Supplier<ItemStack>) () -> {
            ItemStack copy = delegate.get();

            if (copy != null) {
                GrindEnchantmentsDataComponent modComponent = copy.get(GrindEnchantmentsDataComponent.TYPE);
                copy.remove(GrindEnchantmentsDataComponent.TYPE);

                if (modComponent != null) {
                    GrindEnchantments.addLevelCostLore(copy, modComponent::cost, modComponent.canTake());
                }
            }

            return copy;
        });
    }

    @WrapOperation(method = "updateToClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack addLoreOnUpdateToClient(Slot slot, Operation<ItemStack> original) {
        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();
        ItemStack stack = original.call(slot).copy();

        if (!config.dedicatedServerConfig().alternativeCostDisplay()) {
            return stack;
        }

        if (stack != null) {
            GrindEnchantmentsDataComponent modComponent = stack.get(GrindEnchantmentsDataComponent.TYPE);
            stack.remove(GrindEnchantmentsDataComponent.TYPE);

            if (modComponent != null) {
                GrindEnchantments.addLevelCostLore(stack, modComponent::cost, modComponent.canTake());
            }
        }

        return stack;
    }

    @ModifyArg(method = "updateToClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;updateTrackedSlot(ILnet/minecraft/item/ItemStack;Ljava/util/function/Supplier;)V"), index = 2)
    private Supplier<ItemStack> removeAdditionalCopy(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        return () -> stack;
    }

    @ModifyArg(method = "checkCursorStackUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerSyncHandler;updateCursorStack(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/item/ItemStack;)V"), index = 1)
    private ItemStack removeComponentOnCursorStackUpdate(ItemStack stack) {
        if (stack != null) {
            stack.remove(GrindEnchantmentsDataComponent.TYPE);
        }

        return stack;
    }

    @WrapOperation(method = "syncState", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
    private ItemStack removeComponentOnSyncState(ItemStack instance, Operation<ItemStack> original) {
        ItemStack stack = original.call(instance);

        if (stack != null) {
            stack.remove(GrindEnchantmentsDataComponent.TYPE);
        }

        return stack;
    }
}

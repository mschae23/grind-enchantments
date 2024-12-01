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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import de.mschae23.grindenchantments.GrindEnchantments;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.config.GrindEnchantmentsV2Config;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @SuppressWarnings("Guava")
    @Redirect(method = "sendContentUpdates", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;", remap = false))
    private Supplier<ItemStack> onGetCopySupplier(Supplier<ItemStack> supplier) {
        GrindEnchantmentsV2Config config = GrindEnchantmentsMod.getConfig();

        //noinspection ConstantConditions
        if (!((Object) this instanceof GrindstoneScreenHandler) || !config.dedicatedServerConfig().alternativeCostDisplay()) {
            return Suppliers.memoize(supplier);
        }

        return Suppliers.memoize(() -> {
            ItemStack stack = supplier.get();

            if (stack != null) {
                NbtCompound modNbt = stack.getSubNbt(GrindEnchantmentsMod.MODID);

                if (modNbt != null) {
                    int cost;
                    boolean canTake;

                    if (modNbt.contains("Cost", NbtElement.INT_TYPE)) {
                        cost = modNbt.getInt("Cost");
                    } else {
                        return stack;
                    }

                    if (modNbt.contains("CanTake", NbtElement.BYTE_TYPE)) {
                        canTake = modNbt.getBoolean("CanTake");
                    } else {
                        return stack;
                    }

                    return GrindEnchantments.addLevelCostLore(stack, () -> cost, canTake);
                }
            }

            return stack;
        });
    }
}

package de.martenschaefer.grindenchantments.mixin;

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
import de.martenschaefer.grindenchantments.GrindEnchantments;
import de.martenschaefer.grindenchantments.GrindEnchantmentsMod;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsConfig;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @SuppressWarnings("Guava")
    @Redirect(method = "sendContentUpdates", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;"))
    private Supplier<ItemStack> onGetCopySupplier(Supplier<ItemStack> supplier) {
        GrindEnchantmentsConfig config = GrindEnchantmentsMod.getConfig();

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

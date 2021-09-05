package de.martenschaefer.grindenchantments.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import de.martenschaefer.grindenchantments.GrindEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    @Shadow
    @Final
    Inventory input;
    @Final
    @Shadow
    private Inventory result;

    protected GrindstoneScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(at = @At("RETURN"), method = "updateResult", cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        ItemStack itemStack1 = this.input.getStack(0);
        ItemStack itemStack2 = this.input.getStack(1);

        if (GrindEnchantments.Disenchant.isDisenchantOperation(itemStack1, itemStack2)) {
            this.result.setStack(0, GrindEnchantments.Disenchant.doDisenchantOperation(itemStack1, itemStack2));
            this.sendContentUpdates();
            ci.cancel();
        } else if (GrindEnchantments.Move.isMoveOperation(itemStack1, itemStack2)) {
            ItemStack result = GrindEnchantments.Move.doMoveOperation(itemStack1, itemStack2);

            if (result == null) return;

            this.result.setStack(0, result);
            this.sendContentUpdates();
            ci.cancel();
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$2")
    public static class Anonymous2Mixin extends Slot {
        @Shadow
        @Final
        private GrindstoneScreenHandler field_16777;

        public Anonymous2Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
        private void canInsertBooks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Inventory input = ((GrindstoneScreenHandlerAccessor) this.field_16777).getInput();

            cir.setReturnValue(cir.getReturnValueZ() || (stack.getItem() == Items.BOOK
                && input.getStack(1).getItem() != Items.BOOK));
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$3")
    public static class Anonymous3Mixin extends Slot {
        @Shadow
        @Final
        private GrindstoneScreenHandler field_16778;

        public Anonymous3Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
        private void canInsertBooks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Inventory input = ((GrindstoneScreenHandlerAccessor) this.field_16778).getInput();

            cir.setReturnValue(cir.getReturnValueZ() || (stack.getItem() == Items.BOOK
                && input.getStack(0).getItem() != Items.BOOK));
        }
    }

    @Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
    public static abstract class Anonymous4Mixin extends Slot {
        @Shadow
        private ScreenHandlerContext field_16779;
        @Shadow
        @Final
        private GrindstoneScreenHandler field_16780;

        public Anonymous4Mixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        /**
         * @reason I have to change the lambda expression, but I need the player too, which isn't passed to
         * the synthetic method for it.
         * @author mschae23
         */
        @Overwrite
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.field_16779.run((world, pos) -> {
                Inventory input = ((GrindstoneScreenHandlerAccessor) this.field_16780).getInput();

                ItemStack itemStack1 = input.getStack(0);
                ItemStack itemStack2 = input.getStack(1);

                if (GrindEnchantments.Disenchant.isDisenchantOperation(itemStack1, itemStack2)) {
                    GrindEnchantments.Disenchant.takeResult(itemStack1, itemStack2, player, input, world, pos);
                    return;
                } else if (GrindEnchantments.Move.isMoveOperation(itemStack1, itemStack2)) {
                    GrindEnchantments.Move.takeResult(itemStack1, itemStack2, player, input, world, pos);
                    return;
                }

                // Vanilla Grindstone take item logic

                if (world instanceof ServerWorld) {
                    ExperienceOrbEntity.spawn((ServerWorld) world, Vec3d.ofCenter(pos), this.getExperience(world));
                }

                world.syncWorldEvent(1042, pos, 0);

                input.setStack(0, ItemStack.EMPTY);
                input.setStack(1, ItemStack.EMPTY);
            });
        }

        /**
         * @author mschae23
         */
        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            Inventory input = ((GrindstoneScreenHandlerAccessor) this.field_16780).getInput();

            ItemStack itemStack1 = input.getStack(0);
            ItemStack itemStack2 = input.getStack(1);

            if (GrindEnchantments.Disenchant.isDisenchantOperation(itemStack1, itemStack2) ||
                GrindEnchantments.Move.isMoveOperation(itemStack1, itemStack2)) {

                return (playerEntity.getAbilities().creativeMode || playerEntity.experienceLevel >=
                    GrindEnchantments.getLevelCost(itemStack1, itemStack2));
            }

            return true;
        }

        @Shadow
        public abstract int getExperience(World world);
    }
}

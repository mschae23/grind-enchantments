package de.mschae23.grindenchantments.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

public final class GrindstoneEvents {
    public static final Event<CanInsert> CAN_INSERT = EventFactory.createArrayBacked(CanInsert.class, callbacks -> (stack, other, slotId) -> {
        for (CanInsert callback : callbacks) {
            if (callback.canInsert(stack, other, slotId)) {
                return true;
            }
        }

        return false;
    });

    public static final Event<UpdateResult> UPDATE_RESULT = EventFactory.createArrayBacked(UpdateResult.class, callbacks -> (input1, input2, player, wrapperLookup) -> {
        for (UpdateResult callback : callbacks) {
            ItemStack result = callback.onUpdateResult(input1, input2, player, wrapperLookup);

            if (!result.isEmpty()) {
                return result;
            }
        }

        return ItemStack.EMPTY;
    });

    public static final Event<CanTakeResult> CAN_TAKE_RESULT = EventFactory.createArrayBacked(CanTakeResult.class, callbacks -> (input1, input2, player, wrapperLookup) -> {
        for (CanTakeResult callback : callbacks) {
            if (!callback.canTakeResult(input1, input2, player, wrapperLookup)) {
                return false;
            }
        }

        return true;
    });

    public static final Event<TakeResult> TAKE_RESULT = EventFactory.createArrayBacked(TakeResult.class, callbacks -> (input1, input2, resultStack, player, input, wrapperLookup) -> {
        for (TakeResult callback : callbacks) {
            if (callback.onTakeResult(input1, input2, resultStack, player, input, wrapperLookup)) {
                return true;
            }
        }

        return false;
    });

    public static final Event<LevelCost> LEVEL_COST = EventFactory.createArrayBacked(LevelCost.class, callbacks -> (input1, input2, player, wrapperLookup) -> {
        for (LevelCost callback : callbacks) {
            int cost = callback.getLevelCost(input1, input2, player, wrapperLookup);

            if (cost != -1) {
                return cost;
            }
        }

        return -1;
    });

    private GrindstoneEvents() {
    }

    public static <T extends CanInsert & UpdateResult & CanTakeResult & TakeResult & LevelCost> void registerAll(T listener) {
        GrindstoneEvents.CAN_INSERT.register(listener);
        GrindstoneEvents.UPDATE_RESULT.register(listener);
        GrindstoneEvents.CAN_TAKE_RESULT.register(listener);
        GrindstoneEvents.TAKE_RESULT.register(listener);
        GrindstoneEvents.LEVEL_COST.register(listener);
    }

    public interface CanInsert {
        boolean canInsert(ItemStack stack, ItemStack other, int slotId);
    }

    public interface UpdateResult {
        @NotNull
        ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup);
    }

    public interface CanTakeResult {
        boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup);
    }

    public interface TakeResult {
        boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input, RegistryWrapper.WrapperLookup wrapperLookup);
    }

    public interface LevelCost {
        int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player, RegistryWrapper.WrapperLookup wrapperLookup);
    }
}

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

package de.mschae23.grindenchantments.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

public final class GrindstoneEvents {
    public static final Event<CanInsert> CAN_INSERT = EventFactory.createArrayBacked(CanInsert.class, callbacks -> (stack, other, slotId) -> {
        for (CanInsert callback : callbacks) {
            if (callback.canInsert(stack, other, slotId)) {
                return true;
            }
        }

        return false;
    });

    public static final Event<UpdateResult> UPDATE_RESULT = EventFactory.createArrayBacked(UpdateResult.class, callbacks -> (input1, input2, player) -> {
        for (UpdateResult callback : callbacks) {
            ItemStack result = callback.onUpdateResult(input1, input2, player);

            if (result != null) {
                return result;
            }
        }

        return null;
    });

    public static final Event<CanTakeResult> CAN_TAKE_RESULT = EventFactory.createArrayBacked(CanTakeResult.class, callbacks -> (input1, input2, player) -> {
        for (CanTakeResult callback : callbacks) {
            if (!callback.canTakeResult(input1, input2, player)) {
                return false;
            }
        }

        return true;
    });

    public static final Event<TakeResult> TAKE_RESULT = EventFactory.createArrayBacked(TakeResult.class, callbacks -> (input1, input2, resultStack, player, input) -> {
        for (TakeResult callback : callbacks) {
            if (callback.onTakeResult(input1, input2, resultStack, player, input)) {
                return true;
            }
        }

        return false;
    });

    public static final Event<LevelCost> LEVEL_COST = EventFactory.createArrayBacked(LevelCost.class, callbacks -> (input1, input2, player) -> {
        for (LevelCost callback : callbacks) {
            int cost = callback.getLevelCost(input1, input2, player);

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
        @Nullable ItemStack onUpdateResult(ItemStack input1, ItemStack input2, PlayerEntity player);
    }

    public interface CanTakeResult {
        boolean canTakeResult(ItemStack input1, ItemStack input2, PlayerEntity player);
    }

    public interface TakeResult {
        boolean onTakeResult(ItemStack input1, ItemStack input2, ItemStack resultStack, PlayerEntity player, Inventory input);
    }

    public interface LevelCost {
        int getLevelCost(ItemStack input1, ItemStack input2, PlayerEntity player);
    }
}

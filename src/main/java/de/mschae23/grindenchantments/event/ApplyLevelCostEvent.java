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
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import org.apache.logging.log4j.Level;

public final class ApplyLevelCostEvent {
    public static final Identifier DEFAULT = GrindEnchantmentsMod.id("default");
    public static final Identifier MOD_COMPATIBILITY = GrindEnchantmentsMod.id("mod_compatibility");

    public static Event<ApplyLevelCost> EVENT = EventFactory.createWithPhases(ApplyLevelCost.class, callbacks -> (cost, player) -> {
        for (ApplyLevelCost callback : callbacks) {
            if (callback.applyLevelCost(cost, player)) {
                return true;
            }
        }

        GrindEnchantmentsMod.log(Level.INFO, "Could not apply level cost");
        return false;
    }, MOD_COMPATIBILITY, Event.DEFAULT_PHASE, DEFAULT);

    private ApplyLevelCostEvent() {
    }

    public interface ApplyLevelCost {
        boolean applyLevelCost(int cost, PlayerEntity player);
    }
}

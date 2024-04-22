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

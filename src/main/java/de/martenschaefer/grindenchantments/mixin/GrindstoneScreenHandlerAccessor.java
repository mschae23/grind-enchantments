package de.martenschaefer.grindenchantments.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GrindstoneScreenHandler.class)
public interface GrindstoneScreenHandlerAccessor {
    @Accessor("input")
    Inventory getInput();

    @Accessor("result")
    Inventory getResult();
}

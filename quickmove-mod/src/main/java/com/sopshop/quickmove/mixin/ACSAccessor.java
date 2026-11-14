package com.sopshop.quickmove.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ACSAccessor {
    @Accessor("hoveredSlot")
    Slot quickmove$getHoveredSlot();
}

package com.breakinblocks.neosync.mixins.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import com.breakinblocks.neosync.client.gui.controller.DeathScreenController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Gui.class, priority = 1001)
public abstract class GuiMixin {
    /**
     * `setScreen(null)` opens DeathScreen when the player is dead.
     * This method can prevent this from happening.
     */
    @Redirect(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isDeadOrDying()Z", ordinal = 0), require = 1)
    private boolean sync$isPlayerDead(LocalPlayer player) {
        return player.isDeadOrDying() && !DeathScreenController.isSuspended();
    }
}
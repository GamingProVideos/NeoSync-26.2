package com.breakinblocks.neosync.mixins.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import com.breakinblocks.neosync.client.gui.controller.DeathScreenController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handlePlayerCombatKill", at = @At("HEAD"), cancellable = true)
    private void sync$suppressDeathScreen(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        if (DeathScreenController.isSuspended()) {
            ci.cancel();
        }
    }
}

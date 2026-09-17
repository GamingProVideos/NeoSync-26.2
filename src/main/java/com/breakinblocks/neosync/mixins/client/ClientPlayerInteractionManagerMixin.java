package com.breakinblocks.neosync.mixins.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
    /**
     * I truly don't understand why Minecraft doesn't implement this check.
     * Long story short - if the player will try to attack themselves, they will be kicked from a server.
     * And the player actually tries to commit an act of masochism if an attack occurs after their death.
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void sync$attackEntity(Player player, Entity target, CallbackInfo ci) {
        if (player == target) {
            ci.cancel();
        }
    }
}
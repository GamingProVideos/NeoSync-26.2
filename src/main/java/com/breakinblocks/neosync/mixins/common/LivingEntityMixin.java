package com.breakinblocks.neosync.mixins.common;

import net.minecraft.world.entity.LivingEntity;
import com.breakinblocks.neosync.common.entity.KillableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "setHealth", at = @At("RETURN"))
    private void sync$setHealth(float health, CallbackInfo ci) {
        if (health <= 0 && this instanceof KillableEntity) {
            ((KillableEntity)this).onKillableEntityDeath();
        }
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void sync$updatePostDeath(CallbackInfo ci) {
        if (this instanceof KillableEntity && ((KillableEntity)this).updateKillableEntityPostDeath()) {
            ci.cancel();
        }
    }
}
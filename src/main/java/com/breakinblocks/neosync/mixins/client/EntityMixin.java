package com.breakinblocks.neosync.mixins.client;

import net.minecraft.world.entity.Entity;
import com.breakinblocks.neosync.common.entity.LookingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void sync$changeLookDirection(double yaw, double pitch, CallbackInfo ci) {
        if (this instanceof LookingEntity && ((LookingEntity)this).changeLookingEntityLookDirection(yaw, pitch)) {
            ci.cancel();
        }
    }
}
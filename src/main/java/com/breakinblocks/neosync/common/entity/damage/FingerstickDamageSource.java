package com.breakinblocks.neosync.common.entity.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import com.breakinblocks.neosync.NeoSync;

public class FingerstickDamageSource {
    public static final ResourceKey<DamageType> FINGERSTICK = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(NeoSync.MOD_ID, "fingerstick")
    );

    public static DamageSource fingerstick(Level world) {
        return new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(FINGERSTICK));
    }

    public static DamageSource fingerstick(Entity entity) {
        return fingerstick(entity.level());
    }
}
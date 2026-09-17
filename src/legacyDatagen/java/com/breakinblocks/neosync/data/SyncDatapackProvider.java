package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.entity.damage.FingerstickDamageSource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class SyncDatapackProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ctx -> ctx.register(
                    FingerstickDamageSource.FINGERSTICK,
                    new DamageType("neosync.fingerstick", DamageScaling.NEVER, 0.1F)));

    public SyncDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(NeoSync.MOD_ID));
    }
}

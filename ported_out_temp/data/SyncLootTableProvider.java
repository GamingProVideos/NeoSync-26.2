package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SyncLootTableProvider extends LootTableProvider {
    public SyncLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, java.util.Set.of(), List.of(
                new SubProviderEntry(SyncBlockLootSubProvider::new, LootContextParamSets.BLOCK)
        ), registries);
    }
}

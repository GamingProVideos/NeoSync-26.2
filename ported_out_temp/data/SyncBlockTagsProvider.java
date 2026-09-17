package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;

import java.util.concurrent.CompletableFuture;

public final class SyncBlockTagsProvider extends BlockTagsProvider {
    public SyncBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existing) {
        super(output, registries, NeoSync.MOD_ID, existing);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(SyncBlocks.SHELL_STORAGE.get())
                .add(SyncBlocks.SHELL_CONSTRUCTOR.get())
                .add(SyncBlocks.TREADMILL.get());
    }
}

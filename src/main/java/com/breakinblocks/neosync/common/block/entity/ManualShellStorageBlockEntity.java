package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ManualShellStorageBlockEntity extends ShellStorageBlockEntity {
    public ManualShellStorageBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.MANUAL_SHELL_STORAGE.get(), pos, state);
    }

    @Override
    protected boolean isManualOnly() {
        return true;
    }
}

package com.breakinblocks.neosync.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.common.block.entity.ManualShellStorageBlockEntity;

public class ManualShellStorageBlock extends ShellStorageBlock {

    public ManualShellStorageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManualShellStorageBlockEntity(pos, state);
    }
}

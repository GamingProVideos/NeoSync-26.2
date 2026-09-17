package com.breakinblocks.neosync.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.common.block.entity.ZeroPointShellStorageBlockEntity;

public class ZeroPointShellStorageBlock extends ShellStorageBlock {
    public static final MapCodec<ZeroPointShellStorageBlock> CODEC = simpleCodec(ZeroPointShellStorageBlock::new);

    public ZeroPointShellStorageBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends ZeroPointShellStorageBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZeroPointShellStorageBlockEntity(pos, state);
    }
}

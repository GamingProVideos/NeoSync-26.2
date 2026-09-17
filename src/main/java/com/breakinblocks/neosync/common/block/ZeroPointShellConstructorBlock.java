package com.breakinblocks.neosync.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.common.block.entity.ZeroPointShellConstructorBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ZeroPointShellConstructorBlock extends ShellConstructorBlock {

    public ZeroPointShellConstructorBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZeroPointShellConstructorBlockEntity(pos, state);
    }
}

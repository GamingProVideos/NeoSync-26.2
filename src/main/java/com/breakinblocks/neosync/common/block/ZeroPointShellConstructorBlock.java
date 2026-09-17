package com.breakinblocks.neosync.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.common.block.entity.ZeroPointShellConstructorBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ZeroPointShellConstructorBlock extends ShellConstructorBlock {
    public static final MapCodec<ZeroPointShellConstructorBlock> CODEC = simpleCodec(ZeroPointShellConstructorBlock::new);

    public ZeroPointShellConstructorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends ZeroPointShellConstructorBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZeroPointShellConstructorBlockEntity(pos, state);
    }
}

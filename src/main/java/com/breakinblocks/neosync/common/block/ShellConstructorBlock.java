package com.breakinblocks.neosync.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.common.block.entity.ShellConstructorBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ShellConstructorBlock extends AbstractShellContainerBlock {
    public static final MapCodec<ShellConstructorBlock> CODEC = simpleCodec(ShellConstructorBlock::new);

    public ShellConstructorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends ShellConstructorBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShellConstructorBlockEntity(pos, state);
    }
}

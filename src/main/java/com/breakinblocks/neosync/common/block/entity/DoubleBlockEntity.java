package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public interface DoubleBlockEntity {
    DoubleBlockHalf getBlockType(BlockState state);
}
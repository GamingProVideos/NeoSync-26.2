package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class TreadmillRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.SOUTH;
    public boolean isBackPart = true;
    public boolean renderBothHalves;
}

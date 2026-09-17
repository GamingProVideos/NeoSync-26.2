package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neosync.common.block.entity.DoubleBlockEntity;

public abstract class DoubleBlockEntityRenderer<T extends BlockEntity & DoubleBlockEntity, S extends BlockEntityRenderState> implements BlockEntityRenderer<T, S> {
    protected final BlockEntityRendererProvider.Context context;

    public DoubleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }
}

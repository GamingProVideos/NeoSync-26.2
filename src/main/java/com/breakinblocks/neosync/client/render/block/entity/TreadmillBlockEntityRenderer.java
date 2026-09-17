package com.breakinblocks.neosync.client.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.client.model.SyncModelLayers;
import com.breakinblocks.neosync.client.model.TreadmillModel;
import com.breakinblocks.neosync.common.block.TreadmillBlock;
import com.breakinblocks.neosync.common.block.entity.TreadmillBlockEntity;

public class TreadmillBlockEntityRenderer extends DoubleBlockEntityRenderer<TreadmillBlockEntity, TreadmillRenderState> {
    public static final Identifier TEXTURE = NeoSync.locate("textures/block/treadmill.png");

    private final TreadmillModel model;

    public TreadmillBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new TreadmillModel(context.bakeLayer(SyncModelLayers.TREADMILL));
    }

    @Override
    public TreadmillRenderState createRenderState() {
        return new TreadmillRenderState();
    }

    @Override
    public void extractRenderState(TreadmillBlockEntity blockEntity, TreadmillRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        renderState.facing = blockState.getValue(TreadmillBlock.FACING);
        renderState.isBackPart = TreadmillBlock.isBack(blockState);
        renderState.renderBothHalves = false;
    }

    @Override
    public void submit(TreadmillRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        AbstractShellContainerBlockEntityRenderer.applyMachineTransform(poseStack, state.facing);
        submitNodeCollector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }
}

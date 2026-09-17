package com.breakinblocks.neosync.client.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.client.model.ShellContainerModel;
import com.breakinblocks.neosync.client.model.ShellStorageModel;
import com.breakinblocks.neosync.client.model.SyncModelLayers;
import com.breakinblocks.neosync.client.texture.TrimTextureGenerator;
import com.breakinblocks.neosync.common.block.ManualShellStorageBlock;
import com.breakinblocks.neosync.common.block.ZeroPointShellStorageBlock;
import com.breakinblocks.neosync.common.block.entity.ShellEntity;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity;

public class ShellStorageBlockEntityRenderer extends AbstractShellContainerBlockEntityRenderer<ShellStorageBlockEntity> {
    public static final Identifier TEXTURE = NeoSync.locate("textures/block/shell_storage.png");
    public static final Identifier ZERO_POINT_TEXTURE = NeoSync.locate("textures/block/zero_point_shell_storage.png");
    public static final Identifier MANUAL_TEXTURE = NeoSync.locate("textures/block/manual_shell_storage.png");
    private static final TrimTextureGenerator TRIM_TEXTURES = new TrimTextureGenerator(TEXTURE, ZERO_POINT_TEXTURE);

    private final ShellStorageModel model;
    private final Model.Simple ledModel;

    public ShellStorageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ShellStorageModel(context.bakeLayer(SyncModelLayers.SHELL_STORAGE));
        this.ledModel = new Model.Simple(context.bakeLayer(SyncModelLayers.SHELL_STORAGE_LED), RenderTypes::entityCutoutCull);
    }

    @Override
    protected ShellContainerModel getModel() {
        return this.model;
    }

    @Override
    protected Identifier getTexture(ShellStorageBlockEntity blockEntity) {
        DyeColor color = blockEntity.getColor();
        if (blockEntity.getBlockState().getBlock() instanceof ManualShellStorageBlock) {
            return color == null ? MANUAL_TEXTURE : TRIM_TEXTURES.getTexture(color, MANUAL_TEXTURE);
        }
        if (blockEntity.getBlockState().getBlock() instanceof ZeroPointShellStorageBlock) {
            if (color == null || color == DyeColor.CYAN) {
                return ZERO_POINT_TEXTURE;
            }
            return TRIM_TEXTURES.getTexture(color, ZERO_POINT_TEXTURE);
        }
        return TRIM_TEXTURES.getTexture(color, TEXTURE);
    }

    @Override
    public void extractRenderState(ShellStorageBlockEntity blockEntity, ShellContainerRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.ledColor = blockEntity.getIndicatorColor();
        renderState.connectorProgress = blockEntity.getConnectorProgress(partialTick);
    }

    @Override
    protected void poseShell(ShellEntity shell, ShellStorageBlockEntity blockEntity, float partialTick) {
        shell.isActive = true;
        shell.pitchProgress = blockEntity.getConnectorProgress(partialTick);
        holdPose(shell, maxHeadPitch(shell) * shell.pitchProgress);
    }

    @Override
    protected void submitExtras(ShellContainerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (!state.isLowerHalf && !state.renderBothHalves) {
            return;
        }
        int color = 0xFF000000 | (state.ledColor.getTextureDiffuseColor() & 0x00FFFFFF);
        submitNodeCollector.submitModel(this.ledModel, Unit.INSTANCE, poseStack, this.ledModel.renderType(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, 0, state.breakProgress);
    }
}

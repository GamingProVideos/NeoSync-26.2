package com.breakinblocks.neosync.client.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.client.model.ShellContainerModel;
import com.breakinblocks.neosync.client.model.ShellVoxelModel;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.entity.AbstractShellContainerBlockEntity;
import com.breakinblocks.neosync.common.block.entity.ShellEntity;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class AbstractShellContainerBlockEntityRenderer<T extends AbstractShellContainerBlockEntity> extends DoubleBlockEntityRenderer<T, ShellContainerRenderState> {
    public static final Identifier VOXEL_TEXTURE = NeoSync.locate("textures/entity/voxel.png");

    private final Map<AbstractShellContainerBlockEntity, CachedShell> shellEntities = new WeakHashMap<>();
    private final ShellVoxelModel voxelModel;

    public AbstractShellContainerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.voxelModel = new ShellVoxelModel(context.bakeLayer(ModelLayers.PLAYER));
    }

    protected abstract ShellContainerModel getModel();

    protected abstract Identifier getTexture(T blockEntity);

    @Override
    public ShellContainerRenderState createRenderState() {
        return new ShellContainerRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, ShellContainerRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        renderState.facing = blockState.getValue(AbstractShellContainerBlock.FACING);
        renderState.isLowerHalf = blockState.getValue(AbstractShellContainerBlock.HALF) == DoubleBlockHalf.LOWER;
        renderState.renderBothHalves = false;
        renderState.texture = this.getTexture(blockEntity);
        renderState.doorOpenProgress = blockEntity.getDoorOpenProgress(partialTick);

        ShellState shell = blockEntity.getShellState();
        renderState.shellProgress = shell == null ? 0F : shell.getProgress();
        renderState.hasShell = shell != null;

        if (renderState.isLowerHalf && shell != null && shell.getProgress() >= ShellState.PROGRESS_PRINTING) {
            ShellEntity shellEntity = this.getOrCreateClientShellEntity(blockEntity);
            if (shellEntity != null) {
                this.poseShell(shellEntity, blockEntity, partialTick);
                EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                renderState.shellEntityState = dispatcher.extractEntity(shellEntity, partialTick);
            } else {
                renderState.shellEntityState = null;
            }
        } else {
            renderState.shellEntityState = null;
        }
    }

    @Override
    public void submit(ShellContainerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        applyMachineTransform(poseStack, state.facing);
        submitNodeCollector.submitModel(this.getModel(), state, poseStack, state.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        this.submitExtras(state, poseStack, submitNodeCollector);
        poseStack.popPose();

        if (!state.isLowerHalf) {
            return;
        }

        if (state.hasShell && state.shellProgress < ShellState.PROGRESS_DONE) {
            this.submitVoxelShell(state, poseStack, submitNodeCollector);
        }

        if (state.shellEntityState == null) {
            return;
        }

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(facingYaw(state.facing))));
        dispatcher.submit(state.shellEntityState, camera, 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private void submitVoxelShell(ShellContainerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingYaw(state.facing) + 180F));
        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(0.0, -1.501, 0.0);
        submitNodeCollector.submitModel(this.voxelModel, state, poseStack, VOXEL_TEXTURE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }

    protected void submitExtras(ShellContainerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
    }

    protected void poseShell(ShellEntity shell, T blockEntity, float partialTick) {
        shell.isActive = false;
        shell.pitchProgress = 0F;
        holdPose(shell, 0F);
    }

    protected static void holdPose(ShellEntity shell, float pitch) {
        float yaw = shell.getYRot();
        shell.setXRot(pitch);
        shell.xRotO = pitch;
        shell.yRotO = yaw;
        shell.setYHeadRot(yaw);
        shell.yHeadRotO = yaw;
        shell.setYBodyRot(yaw);
        shell.yBodyRotO = yaw;
    }

    protected static float maxHeadPitch(ShellEntity shell) {
        return shell.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? 15F : 5F;
    }

    public static void applyMachineTransform(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.scale(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
    }

    @Nullable
    protected ShellEntity getOrCreateClientShellEntity(AbstractShellContainerBlockEntity blockEntity) {
        AbstractShellContainerBlockEntity bottom = blockEntity.getBottomPart().orElse(null);
        if (bottom == null) {
            return null;
        }
        ShellState current = bottom.getShellState();
        if (current == null) {
            this.shellEntities.remove(bottom);
            return null;
        }
        CachedShell cached = this.shellEntities.get(bottom);
        if (cached == null || cached.source != current) {
            cached = new CachedShell(current, new ShellEntity(current));
            this.shellEntities.put(bottom, cached);
        }
        return cached.entity;
    }

    private static float facingYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 180F;
            case EAST -> 270F;
            case WEST -> 90F;
            default -> 0F;
        };
    }

    private record CachedShell(ShellState source, ShellEntity entity) {}
}

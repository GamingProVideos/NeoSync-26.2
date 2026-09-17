package com.breakinblocks.neosync.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import com.breakinblocks.neosync.client.model.ShellContainerModel;
import com.breakinblocks.neosync.client.render.block.entity.AbstractShellContainerBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.ShellContainerRenderState;

import java.util.function.Consumer;

public class ShellContainerSpecialRenderer implements NoDataSpecialModelRenderer {
    private final ShellContainerModel model;
    @Nullable
    private final Model.Simple ledModel;
    private final Identifier texture;
    private final ShellContainerRenderState state;

    public ShellContainerSpecialRenderer(ShellContainerModel model, @Nullable Model.Simple ledModel, Identifier texture) {
        this.model = model;
        this.ledModel = ledModel;
        this.texture = texture;
        this.state = new ShellContainerRenderState();
        this.state.renderBothHalves = true;
        this.state.texture = texture;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        AbstractShellContainerBlockEntityRenderer.applyMachineTransform(poseStack, Direction.SOUTH);
        submitNodeCollector.submitModel(this.model, this.state, poseStack, this.texture, lightCoords, overlayCoords, outlineColor, null);
        if (this.ledModel != null) {
            int color = 0xFF000000 | (this.state.ledColor.getTextureDiffuseColor() & 0x00FFFFFF);
            submitNodeCollector.submitModel(this.ledModel, Unit.INSTANCE, poseStack, this.ledModel.renderType(this.texture),
                    lightCoords, overlayCoords, color, null, outlineColor, null);
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        AbstractShellContainerBlockEntityRenderer.applyMachineTransform(poseStack, Direction.SOUTH);
        this.model.setupAnim(this.state);
        this.model.root().getExtentsForGui(poseStack, output);
    }
}

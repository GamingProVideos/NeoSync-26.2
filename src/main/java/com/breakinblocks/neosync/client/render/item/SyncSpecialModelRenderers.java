package com.breakinblocks.neosync.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;
import com.breakinblocks.neosync.client.model.ShellConstructorModel;
import com.breakinblocks.neosync.client.model.ShellStorageModel;
import com.breakinblocks.neosync.client.model.SyncModelLayers;
import com.breakinblocks.neosync.client.model.TreadmillModel;
import com.breakinblocks.neosync.client.render.block.entity.AbstractShellContainerBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.TreadmillRenderState;

import java.util.function.Consumer;

public final class SyncSpecialModelRenderers {
    private SyncSpecialModelRenderers() {}

    public record ShellStorage(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<ShellStorage> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Identifier.CODEC.fieldOf("texture").forGetter(ShellStorage::texture)).apply(i, ShellStorage::new));

        @Override
        public MapCodec<ShellStorage> type() {
            return MAP_CODEC;
        }

        @Override
        public ShellContainerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new ShellContainerSpecialRenderer(
                    new ShellStorageModel(context.entityModelSet().bakeLayer(SyncModelLayers.SHELL_STORAGE)),
                    new Model.Simple(context.entityModelSet().bakeLayer(SyncModelLayers.SHELL_STORAGE_LED), RenderTypes::entityCutoutCull),
                    this.texture);
        }
    }

    public record ShellConstructor(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<ShellConstructor> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Identifier.CODEC.fieldOf("texture").forGetter(ShellConstructor::texture)).apply(i, ShellConstructor::new));

        @Override
        public MapCodec<ShellConstructor> type() {
            return MAP_CODEC;
        }

        @Override
        public ShellContainerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new ShellContainerSpecialRenderer(
                    new ShellConstructorModel(context.entityModelSet().bakeLayer(SyncModelLayers.SHELL_CONSTRUCTOR)),
                    null,
                    this.texture);
        }
    }

    public record Treadmill(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Treadmill> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Identifier.CODEC.fieldOf("texture").forGetter(Treadmill::texture)).apply(i, Treadmill::new));

        @Override
        public MapCodec<Treadmill> type() {
            return MAP_CODEC;
        }

        @Override
        public TreadmillSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TreadmillSpecialRenderer(new TreadmillModel(context.entityModelSet().bakeLayer(SyncModelLayers.TREADMILL)), this.texture);
        }
    }

    public static class TreadmillSpecialRenderer implements NoDataSpecialModelRenderer {
        private final TreadmillModel model;
        private final Identifier texture;
        private final TreadmillRenderState state;

        public TreadmillSpecialRenderer(TreadmillModel model, Identifier texture) {
            this.model = model;
            this.texture = texture;
            this.state = new TreadmillRenderState();
            this.state.renderBothHalves = true;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            poseStack.pushPose();
            AbstractShellContainerBlockEntityRenderer.applyMachineTransform(poseStack, Direction.SOUTH);
            submitNodeCollector.submitModel(this.model, this.state, poseStack, this.texture, lightCoords, overlayCoords, outlineColor, null);
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
}

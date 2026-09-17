package com.breakinblocks.neosync.client.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public record AnnulusSectorRenderState(
        Matrix3x2fc pose,
        float centerX,
        float centerY,
        float innerRadius,
        float outerRadius,
        float fromAngle,
        float toAngle,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    private static final float MAX_STEP = Mth.PI / 64F;

    public AnnulusSectorRenderState(Matrix3x2f pose, float centerX, float centerY, float innerRadius, float outerRadius,
                                    float fromAngle, float toAngle, int color, @Nullable ScreenRectangle scissorArea) {
        this(pose, centerX, centerY, innerRadius, outerRadius, fromAngle, toAngle, color, scissorArea,
                computeBounds(centerX, centerY, outerRadius, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        float span = this.toAngle - this.fromAngle;
        int steps = Math.max(1, Mth.ceil(Math.abs(span) / MAX_STEP));
        float step = span / steps;

        for (int i = 0; i < steps; ++i) {
            float a0 = this.fromAngle + step * i;
            float a1 = a0 + step;
            float cos0 = Mth.cos(a0);
            float sin0 = Mth.sin(a0);
            float cos1 = Mth.cos(a1);
            float sin1 = Mth.sin(a1);

            vertexConsumer.addVertexWith2DPose(this.pose, this.centerX + cos1 * this.innerRadius, this.centerY + sin1 * this.innerRadius).setColor(this.color);
            vertexConsumer.addVertexWith2DPose(this.pose, this.centerX + cos1 * this.outerRadius, this.centerY + sin1 * this.outerRadius).setColor(this.color);
            vertexConsumer.addVertexWith2DPose(this.pose, this.centerX + cos0 * this.outerRadius, this.centerY + sin0 * this.outerRadius).setColor(this.color);
            vertexConsumer.addVertexWith2DPose(this.pose, this.centerX + cos0 * this.innerRadius, this.centerY + sin0 * this.innerRadius).setColor(this.color);
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Nullable
    private static ScreenRectangle computeBounds(float centerX, float centerY, float outerRadius, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        int size = Mth.ceil(outerRadius * 2F) + 2;
        ScreenRectangle rectangle = new ScreenRectangle(Mth.floor(centerX - outerRadius) - 1, Mth.floor(centerY - outerRadius) - 1, size, size)
                .transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(rectangle) : rectangle;
    }
}

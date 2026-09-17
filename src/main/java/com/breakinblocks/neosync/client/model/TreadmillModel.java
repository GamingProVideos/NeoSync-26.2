package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.breakinblocks.neosync.client.render.block.entity.TreadmillRenderState;
import com.breakinblocks.neosync.common.utils.math.Radians;

public class TreadmillModel extends Model<TreadmillRenderState> {
    private static final float FRONT_ITEM_OFFSET = 32F;
    private static final float PITCH = Radians.R_PI / 72F;
    private static final float CONSOLE_PITCH = 1.134464F;

    private final ModelPart back;
    private final ModelPart front;

    public TreadmillModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
        this.back = root.getChild("back");
        this.front = root.getChild("front");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition back = root.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition front = root.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.ZERO);

        front.addOrReplaceChild("running_belt", box(31, 0, 18F, 4F, 31F), pitched(-9F, 17F, -16F));
        back.addOrReplaceChild("running_belt", box(31, 31, 18F, 4F, 31F), pitched(-9F, 18.3F, -14F));

        front.addOrReplaceChild("console", box(0, 3, 26F, 10F, 2F),
                PartPose.offsetAndRotation(13F, 6F, 16F, CONSOLE_PITCH, Radians.R_PI, 0F));
        front.addOrReplaceChild("console_mast_left", box(0, 66, 1F, 15F, 2F), PartPose.offset(12.5F, 7F, 10F));
        front.addOrReplaceChild("console_mast_right", box(0, 66, 1F, 15F, 2F), PartPose.offset(-13.5F, 7F, 10F));

        front.addOrReplaceChild("lift_arm_front", box(0, 0, 25F, 2F, 1F), mirrored(12.5F, 24F, 15F));
        front.addOrReplaceChild("lift_arm_left", box(0, 66, 1F, 2F, 34F), mirrored(13.5F, 24F, -18F));
        front.addOrReplaceChild("lift_arm_right", box(0, 66, 1F, 2F, 34F), mirrored(-12.5F, 24F, -18F));

        front.addOrReplaceChild("side_reinforcement_left", box(130, 35, 4F, 5F, 32F), pitched(9F, 16.5F, -16.1F));
        back.addOrReplaceChild("side_reinforcement_left", box(130, 35, 4F, 5F, 32F), pitched(9F, 17.9F, -16F));
        front.addOrReplaceChild("side_reinforcement_right", box(130, 35, 4F, 5F, 32F), pitched(-13F, 16.5F, -16.1F));
        back.addOrReplaceChild("side_reinforcement_right", box(130, 35, 4F, 5F, 32F), pitched(-13F, 17.9F, -16F));

        front.addOrReplaceChild("side_guard_support_right_top", box(86, 88, 1F, 1F, 16F), pitched(12.5F, 6.5F, -15F));
        back.addOrReplaceChild("side_guard_support_right_top", box(86, 88, 1F, 1F, 16F), pitched(12.5F, 7.2F, 1.05F));
        front.addOrReplaceChild("side_guard_support_left_top", box(86, 88, 1F, 1F, 16F), pitched(-13.5F, 6.5F, -15F));
        back.addOrReplaceChild("side_guard_support_left_top", box(86, 88, 1F, 1F, 16F), pitched(-13.5F, 7.2F, 1.05F));

        front.addOrReplaceChild("side_guard_support_right", box(146, 0, 1F, 11F, 1F), pitched(12.5F, 6F, 0F));
        back.addOrReplaceChild("side_guard_support_right", box(146, 0, 1F, 11F, 1F), pitched(12.5F, 7.2F, 1.05F));
        front.addOrReplaceChild("side_guard_support_left", box(146, 0, 1F, 11F, 1F), pitched(-13.5F, 6F, 0F));
        back.addOrReplaceChild("side_guard_support_left", box(146, 0, 1F, 11F, 1F), pitched(-13.5F, 7.2F, 1.05F));

        front.addOrReplaceChild("side_guard_left", box(210, 15, 0F, 10F, 15F), pitched(-13.1F, 7.35F, -15F));
        back.addOrReplaceChild("side_guard_left", box(180, 15, 0F, 10F, 15F), pitched(-13.1F, 8F, 2F));
        front.addOrReplaceChild("side_guard_right", box(210, 15, 0F, 10F, 15F), pitched(13.1F, 7.35F, -15F));
        back.addOrReplaceChild("side_guard_right", box(180, 15, 0F, 10F, 15F), pitched(13.1F, 8F, 2F));

        return LayerDefinition.create(mesh, 256, 128);
    }

    private static CubeListBuilder box(int u, int v, float width, float height, float depth) {
        return CubeListBuilder.create().mirror().texOffs(u, v).addBox(0F, 0F, 0F, width, height, depth);
    }

    private static PartPose pitched(float x, float y, float z) {
        return PartPose.offsetAndRotation(x, y, z, PITCH, 0F, 0F);
    }

    private static PartPose mirrored(float x, float y, float z) {
        return PartPose.offsetAndRotation(x, y, z, 0F, 0F, Radians.R_PI);
    }

    @Override
    public void setupAnim(TreadmillRenderState state) {
        this.back.visible = state.renderBothHalves || state.isBackPart;
        this.front.visible = state.renderBothHalves || !state.isBackPart;
        this.front.z = state.renderBothHalves ? FRONT_ITEM_OFFSET : 0F;
    }
}

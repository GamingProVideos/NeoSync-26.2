package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import com.breakinblocks.neosync.client.render.block.entity.ShellContainerRenderState;

public class ShellStorageModel extends ShellContainerModel {
    private static final float LED_PITCH = -0.4833219F;

    private final ModelPart headConnector;

    public ShellStorageModel(ModelPart root) {
        super(root);
        this.headConnector = this.upper.getChild("head_connector");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition lower = root.addOrReplaceChild("lower", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition upper = root.addOrReplaceChild("upper", CubeListBuilder.create(), PartPose.ZERO);

        lower.addOrReplaceChild("floor", box(64, 62, 32F, 1F, 32F), PartPose.offset(-16F, 23F, -16F));
        lower.addOrReplaceChild("floor_rim_front", box(70, 34, 26F, 1F, 3F), PartPose.offset(-13F, 22F, -14F));
        lower.addOrReplaceChild("floor_rim_left", box(0, 38, 3F, 1F, 27F), PartPose.offset(13F, 22F, -14F));
        lower.addOrReplaceChild("floor_rim_right", box(0, 38, 3F, 1F, 27F), PartPose.offset(-16F, 22F, -14F));
        lower.addOrReplaceChild("floor_rim_back", box(0, 34, 32F, 1F, 3F), PartPose.offset(-16F, 22F, 13F));

        lower.addOrReplaceChild("feet_retainer_front", box(70, 100, 18F, 3F, 1F), PartPose.offset(-9F, 16F, -4.75F));
        lower.addOrReplaceChild("feet_retainer_back", box(70, 100, 18F, 3F, 1F), PartPose.offset(-9F, 16F, 4.25F));
        lower.addOrReplaceChild("feet_retainer_right", box(70, 104, 1F, 3F, 8F), PartPose.offset(-9F, 16F, -3.75F));
        lower.addOrReplaceChild("feet_retainer_left", box(70, 104, 1F, 3F, 8F), PartPose.offset(8F, 16F, -3.75F));
        lower.addOrReplaceChild("feet_retainer_support_right", box(70, 115, 1F, 9F, 2F), PartPose.offset(-9F, 16.75F, -0.75F));
        lower.addOrReplaceChild("feet_retainer_support_left", box(70, 115, 1F, 9F, 2F), PartPose.offset(8F, 16.75F, -0.75F));

        lower.addOrReplaceChild("wall_left", box(200, 122, 1F, 30F, 27F), PartPose.offset(15.05F, -8F, -14F));
        lower.addOrReplaceChild("wall_right", box(200, 122, 1F, 30F, 27F), PartPose.offset(-16.05F, -8F, -14F));
        lower.addOrReplaceChild("wall_back", box(96, 161, 26F, 30F, 2F), PartPose.offset(-13F, -8F, 14F));

        lower.addOrReplaceChild("pillar_front_right", box(60, 68, 1F, 31F, 1F), PartPose.offset(-16F, -8F, -15F));
        lower.addOrReplaceChild("pillar_front_left", box(60, 68, 1F, 31F, 1F), PartPose.offset(15F, -8F, -15F));
        lower.addOrReplaceChild("pillar_back_right", box(0, 155, 3F, 30F, 3F), PartPose.offset(-16F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_left", box(0, 155, 3F, 30F, 3F), PartPose.offset(13F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_1", box(88, 157, 3F, 30F, 1F), PartPose.offset(-10.5F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_2", box(88, 157, 3F, 30F, 1F), PartPose.offset(-4.5F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_3", box(88, 157, 3F, 30F, 1F), PartPose.offset(1.5F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_4", box(88, 157, 3F, 30F, 1F), PartPose.offset(7.5F, -8F, 13F));

        lower.addOrReplaceChild("led_bottom", box(0, 0, 5F, 1F, 1F), PartPose.offsetAndRotation(8F, 21.5F, 9.335F, LED_PITCH, 0F, 0F));
        lower.addOrReplaceChild("led_top", box(0, 0, 5F, 1F, 1F), PartPose.offsetAndRotation(8F, 16.2F, 12.12F, LED_PITCH, 0F, 0F));
        lower.addOrReplaceChild("led_right", box(0, 0, 1F, 6F, 1F), PartPose.offsetAndRotation(8F, 17F, 11.7F, LED_PITCH, 0F, 0F));
        lower.addOrReplaceChild("led_left", box(0, 0, 1F, 6F, 1F), PartPose.offsetAndRotation(12F, 17F, 11.7F, LED_PITCH, 0F, 0F));

        upper.addOrReplaceChild("ceiling", box(0, 0, 32F, 2F, 32F), PartPose.offset(-16F, -8F, -16F));
        upper.addOrReplaceChild("ceiling_rim_front", box(70, 34, 26F, 1F, 3F), PartPose.offset(-13F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_left", box(0, 38, 3F, 1F, 27F), PartPose.offset(13F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_right", box(0, 38, 3F, 1F, 27F), PartPose.offset(-16F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_back", box(0, 34, 32F, 1F, 3F), PartPose.offset(-16F, -6F, 13F));

        upper.addOrReplaceChild("shoulder_retainer_left", box(70, 107, 1F, 1F, 19F), PartPose.offset(6.5F, 16F, -5F));
        upper.addOrReplaceChild("shoulder_retainer_left_top", box(78, 115, 1F, 4F, 1F), PartPose.offset(6.5F, 13F, -5F));
        upper.addOrReplaceChild("shoulder_retainer_right", box(70, 107, 1F, 1F, 19F), PartPose.offset(-7.5F, 16F, -5F));
        upper.addOrReplaceChild("shoulder_retainer_right_top", box(78, 115, 1F, 4F, 1F), PartPose.offset(-7.5F, 13F, -5F));

        upper.addOrReplaceChild("head_retainer_left", box(70, 127, 1F, 1F, 6F), PartPose.offset(0.5F, 3F, 9F));
        upper.addOrReplaceChild("head_retainer_top", box(70, 134, 3F, 1F, 6F), PartPose.offset(-1.5F, 2F, 9F));
        upper.addOrReplaceChild("head_retainer_bottom", box(70, 134, 3F, 1F, 6F), PartPose.offset(-1.5F, 4F, 9F));
        upper.addOrReplaceChild("head_retainer_right", box(70, 127, 1F, 1F, 6F), PartPose.offset(-1.5F, 3F, 9F));
        upper.addOrReplaceChild("head_connector", box(70, 141, 1F, 1F, 5F), PartPose.offset(-0.5F, 3F, 10F));

        upper.addOrReplaceChild("wall_left", box(200, 66, 1F, 29F, 27F), PartPose.offset(15.05F, -5F, -14F));
        upper.addOrReplaceChild("wall_right", box(200, 66, 1F, 29F, 27F), PartPose.offset(-16.05F, -5F, -14F));
        upper.addOrReplaceChild("wall_back", box(96, 128, 26F, 29F, 2F), PartPose.offset(-13F, -5F, 14F));

        upper.addOrReplaceChild("pillar_front_right", box(60, 38, 1F, 30F, 1F), PartPose.offset(-16F, -6F, -15F));
        upper.addOrReplaceChild("pillar_front_left", box(60, 38, 1F, 30F, 1F), PartPose.offset(15F, -6F, -15F));
        upper.addOrReplaceChild("pillar_back_right", box(0, 126, 3F, 29F, 3F), PartPose.offset(-16F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_left", box(0, 126, 3F, 29F, 3F), PartPose.offset(13F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_1", box(88, 128, 3F, 29F, 1F), PartPose.offset(-10.5F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_2", box(88, 128, 3F, 29F, 1F), PartPose.offset(-4.5F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_3", box(88, 128, 3F, 29F, 1F), PartPose.offset(1.5F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_4", box(88, 128, 3F, 29F, 1F), PartPose.offset(7.5F, -5F, 13F));

        addDoors(lower, upper);
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createLedLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("led_light", box(0, 9, 5F, 8F, 1F),
                PartPose.offsetAndRotation(8F, 17.15F, 11.8F, LED_PITCH, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static CubeListBuilder box(int u, int v, float width, float height, float depth) {
        return CubeListBuilder.create().mirror().texOffs(u, v).addBox(0F, 0F, 0F, width, height, depth);
    }

    @Override
    public void setupAnim(ShellContainerRenderState state) {
        super.setupAnim(state);
        this.headConnector.z = 10F - 5F * state.connectorProgress;
    }
}

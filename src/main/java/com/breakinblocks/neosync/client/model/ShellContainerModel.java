package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.breakinblocks.neosync.client.render.block.entity.ShellContainerRenderState;
import com.breakinblocks.neosync.common.utils.math.Radians;

public abstract class ShellContainerModel extends Model<ShellContainerRenderState> {
    public static final float UPPER_ITEM_OFFSET = -32F;

    protected final ModelPart lower;
    protected final ModelPart upper;

    private final ModelPart lowerDoorLeft;
    private final ModelPart lowerDoorRight;
    private final ModelPart upperDoorLeft;
    private final ModelPart upperDoorRight;

    protected ShellContainerModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
        this.lower = root.getChild("lower");
        this.upper = root.getChild("upper");
        this.lowerDoorLeft = this.lower.getChild("door_left");
        this.lowerDoorRight = this.lower.getChild("door_right");
        this.upperDoorLeft = this.upper.getChild("door_left");
        this.upperDoorRight = this.upper.getChild("door_right");
    }

    protected static void addDoors(PartDefinition lower, PartDefinition upper) {
        lower.addOrReplaceChild("door_left",
                CubeListBuilder.create().mirror().texOffs(224, 32).addBox(0F, 0F, 0F, 15F, 31F, 1F),
                PartPose.offset(-15F, -8F, -15F));
        lower.addOrReplaceChild("door_right",
                CubeListBuilder.create().mirror().texOffs(224, 32).addBox(0F, 0F, 0F, 15F, 31F, 1F),
                PartPose.offsetAndRotation(15F, -8F, -14F, 0F, Radians.R_PI, 0F));

        upper.addOrReplaceChild("door_left",
                CubeListBuilder.create().mirror().texOffs(224, 0).addBox(0F, 0F, 0F, 15F, 30F, 1F),
                PartPose.offset(-15F, -6F, -15F));
        upper.addOrReplaceChild("door_right",
                CubeListBuilder.create().mirror().texOffs(224, 0).addBox(0F, 0F, 0F, 15F, 30F, 1F),
                PartPose.offsetAndRotation(15F, -6F, -14F, 0F, Radians.R_PI, 0F));
    }

    @Override
    public void setupAnim(ShellContainerRenderState state) {
        this.lower.visible = state.renderBothHalves || state.isLowerHalf;
        this.upper.visible = state.renderBothHalves || !state.isLowerHalf;
        this.upper.y = state.renderBothHalves ? UPPER_ITEM_OFFSET : 0F;

        float open = state.doorOpenProgress;
        this.lowerDoorLeft.yRot = this.upperDoorLeft.yRot = Radians.R_PI_2 * open;
        this.lowerDoorRight.yRot = this.upperDoorRight.yRot = Radians.R_PI - Radians.R_PI_2 * open;

        this.lowerDoorLeft.z = -15F + 15F * open;
        this.upperDoorLeft.z = -15F + 15F * open;
        this.lowerDoorRight.z = -14F + 14F * open;
        this.upperDoorRight.z = -14F + 14F * open;
        this.lowerDoorRight.x = this.upperDoorRight.x = 15F - open;
    }
}

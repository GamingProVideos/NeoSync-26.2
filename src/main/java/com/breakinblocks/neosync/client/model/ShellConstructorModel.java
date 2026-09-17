package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.client.render.block.entity.ShellContainerRenderState;
import com.breakinblocks.neosync.common.utils.math.Radians;

public class ShellConstructorModel extends ShellContainerModel {
    private static final float MIN_SPRAYER_Y = 22F;
    private static final float SPRAYER_HEIGHT = -60F;
    private static final float MIN_PRINTER_Y = 20F;
    private static final float PRINTER_HEIGHT = -58F;
    private static final float MAST_HEIGHT = 32F;
    private static final float MAST_Y = MIN_SPRAYER_Y - MAST_HEIGHT;
    private static final float SPRAYER_ACTIVATION_STAGE = 0.9F;

    private final ModelPart inner;
    private final ModelPart printer;
    private final ModelPart sprayerMasts;
    private final ModelPart sprayerMastsBase;
    private final ModelPart sprayerHeads;

    public ShellConstructorModel(ModelPart root) {
        super(root);
        this.inner = this.lower.getChild("inner");
        this.printer = this.inner.getChild("printer");
        this.sprayerMasts = this.inner.getChild("sprayer_masts");
        this.sprayerMastsBase = this.inner.getChild("sprayer_masts_base");
        this.sprayerHeads = this.inner.getChild("sprayer_heads");
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

        lower.addOrReplaceChild("wall_left", box(200, 122, 1F, 30F, 27F), PartPose.offset(15.05F, -8F, -14F));
        lower.addOrReplaceChild("wall_right", box(200, 122, 1F, 30F, 27F), PartPose.offset(-16.05F, -8F, -14F));
        lower.addOrReplaceChild("wall_back", box(101, 188, 26F, 30F, 1F), PartPose.offset(-13F, -8F, 14F));

        lower.addOrReplaceChild("pillar_front_left", box(60, 68, 1F, 31F, 1F), PartPose.offset(-16F, -8F, -15F));
        lower.addOrReplaceChild("pillar_front_right", box(60, 68, 1F, 31F, 1F), PartPose.offset(15F, -8F, -15F));
        lower.addOrReplaceChild("pillar_back_left", box(0, 155, 3F, 30F, 3F), PartPose.offset(-16F, -8F, 13F));
        lower.addOrReplaceChild("pillar_back_right", box(0, 155, 3F, 30F, 3F), PartPose.offset(13F, -8F, 13F));

        upper.addOrReplaceChild("ceiling", box(0, 0, 32F, 2F, 32F), PartPose.offset(-16F, -8F, -16F));
        upper.addOrReplaceChild("ceiling_rim_front", box(70, 34, 26F, 1F, 3F), PartPose.offset(-13F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_left", box(0, 38, 3F, 1F, 27F), PartPose.offset(13F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_right", box(0, 38, 3F, 1F, 27F), PartPose.offset(-16F, -6F, -14F));
        upper.addOrReplaceChild("ceiling_rim_back", box(0, 34, 32F, 1F, 3F), PartPose.offset(-16F, -6F, 13F));

        upper.addOrReplaceChild("wall_left", box(200, 66, 1F, 29F, 27F), PartPose.offset(15.05F, -5F, -14F));
        upper.addOrReplaceChild("wall_right", box(200, 66, 1F, 29F, 27F), PartPose.offset(-16.05F, -5F, -14F));
        upper.addOrReplaceChild("wall_back", box(101, 156, 26F, 29F, 1F), PartPose.offset(-13F, -5F, 14F));

        upper.addOrReplaceChild("pillar_front_left", box(60, 38, 1F, 30F, 1F), PartPose.offset(-16F, -6F, -15F));
        upper.addOrReplaceChild("pillar_front_right", box(60, 38, 1F, 30F, 1F), PartPose.offset(15F, -6F, -15F));
        upper.addOrReplaceChild("pillar_back_left", box(0, 126, 3F, 29F, 3F), PartPose.offset(-16F, -5F, 13F));
        upper.addOrReplaceChild("pillar_back_right", box(0, 126, 3F, 29F, 3F), PartPose.offset(13F, -5F, 13F));

        PartDefinition inner = lower.addOrReplaceChild("inner", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition printer = inner.addOrReplaceChild("printer", CubeListBuilder.create(), PartPose.ZERO);
        printer.addOrReplaceChild("printer_left", box(54, 66, 1F, 2F, 2F), PartPose.offsetAndRotation(13F, 0F, 12F, 0F, -Radians.R_PI_4, 0F));
        printer.addOrReplaceChild("printer_right", box(54, 66, 1F, 2F, 2F), PartPose.offsetAndRotation(-14F, 0F, 12F, 0F, Radians.R_PI_4, 0F));

        addMasts(inner.addOrReplaceChild("sprayer_masts", CubeListBuilder.create(), PartPose.ZERO));
        addMasts(inner.addOrReplaceChild("sprayer_masts_base", CubeListBuilder.create(), PartPose.offset(0F, MAST_Y, 0F)));

        PartDefinition heads = inner.addOrReplaceChild("sprayer_heads", CubeListBuilder.create(), PartPose.ZERO);
        heads.addOrReplaceChild("sprayer_red", box(132, 0, 2F, 1F, 2F), PartPose.offsetAndRotation(-12F, 0F, -9.55F, 0F, Radians.R_PI_4, 0F));
        heads.addOrReplaceChild("sprayer_green", box(132, 0, 2F, 1F, 2F), PartPose.offsetAndRotation(10F, 0F, -9.55F, 0F, -Radians.R_PI_4, 0F));
        heads.addOrReplaceChild("sprayer_blue", box(132, 0, 2F, 1F, 2F), PartPose.offset(-1F, 0F, 10.55F));

        addDoors(lower, upper);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void addMasts(PartDefinition masts) {
        masts.addOrReplaceChild("mast_red", box(128, 0, 1F, 32F, 1F), PartPose.offsetAndRotation(-11.5F, 0F, -9.5F, 0F, Radians.R_PI_4, 0F));
        masts.addOrReplaceChild("mast_green", box(128, 0, 1F, 32F, 1F), PartPose.offsetAndRotation(10.5F, 0F, -9.5F, 0F, -Radians.R_PI_4, 0F));
        masts.addOrReplaceChild("mast_blue", box(128, 0, 1F, 32F, 1F), PartPose.offset(-0.5F, 0F, 11.5F));
    }

    private static CubeListBuilder box(int u, int v, float width, float height, float depth) {
        return CubeListBuilder.create().mirror().texOffs(u, v).addBox(0F, 0F, 0F, width, height, depth);
    }

    @Override
    public void setupAnim(ShellContainerRenderState state) {
        super.setupAnim(state);

        this.inner.visible = state.showInnerParts;
        if (!state.showInnerParts) {
            return;
        }

        float printingProgress = state.shellProgress / ShellState.PROGRESS_PRINTING;
        float paintingProgress = (state.shellProgress - ShellState.PROGRESS_PRINTING) / ShellState.PROGRESS_PAINTING;

        float printerY = MIN_PRINTER_Y + PRINTER_HEIGHT * (printingProgress <= 1F ? printingProgress : (1F - paintingProgress));
        float sprayerY = MIN_SPRAYER_Y + SPRAYER_HEIGHT * (printingProgress < SPRAYER_ACTIVATION_STAGE
                ? 0F
                : printingProgress <= 1F
                        ? ((printingProgress - SPRAYER_ACTIVATION_STAGE) / (1F - SPRAYER_ACTIVATION_STAGE))
                        : (1F - paintingProgress));

        this.printer.y = printerY;
        this.sprayerMasts.y = sprayerY;
        this.sprayerHeads.y = sprayerY - 0.5F;
        this.sprayerMastsBase.visible = sprayerY < MAST_Y;
    }
}

package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.client.model.ShellConstructorModel;
import com.breakinblocks.neosync.client.model.ShellContainerModel;
import com.breakinblocks.neosync.client.model.SyncModelLayers;
import com.breakinblocks.neosync.client.texture.TrimTextureGenerator;
import com.breakinblocks.neosync.common.block.ZeroPointShellConstructorBlock;
import com.breakinblocks.neosync.common.block.entity.ShellConstructorBlockEntity;

public class ShellConstructorBlockEntityRenderer extends AbstractShellContainerBlockEntityRenderer<ShellConstructorBlockEntity> {
    public static final Identifier TEXTURE = NeoSync.locate("textures/block/shell_constructor.png");
    public static final Identifier ZERO_POINT_TEXTURE = NeoSync.locate("textures/block/zero_point_shell_constructor.png");
    private static final TrimTextureGenerator TRIM_TEXTURES = new TrimTextureGenerator(TEXTURE, ZERO_POINT_TEXTURE);

    private final ShellConstructorModel model;

    public ShellConstructorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ShellConstructorModel(context.bakeLayer(SyncModelLayers.SHELL_CONSTRUCTOR));
    }

    @Override
    protected ShellContainerModel getModel() {
        return this.model;
    }

    @Override
    protected Identifier getTexture(ShellConstructorBlockEntity blockEntity) {
        DyeColor color = blockEntity.getColor();
        if (blockEntity.getBlockState().getBlock() instanceof ZeroPointShellConstructorBlock) {
            if (color == null || color == DyeColor.CYAN) {
                return ZERO_POINT_TEXTURE;
            }
            return TRIM_TEXTURES.getTexture(color, ZERO_POINT_TEXTURE);
        }
        return TRIM_TEXTURES.getTexture(color, TEXTURE);
    }

    @Override
    public void extractRenderState(ShellConstructorBlockEntity blockEntity, ShellContainerRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.showInnerParts = true;
    }
}

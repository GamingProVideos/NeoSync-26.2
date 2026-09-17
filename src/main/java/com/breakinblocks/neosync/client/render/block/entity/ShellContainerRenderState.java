package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.NeoSync;

public class ShellContainerRenderState extends BlockEntityRenderState {
    @Nullable
    public EntityRenderState shellEntityState;
    public Identifier texture = NeoSync.locate("textures/block/shell_storage.png");
    public Direction facing = Direction.SOUTH;
    public boolean isLowerHalf = true;
    public boolean renderBothHalves;
    public float doorOpenProgress;
    public float shellProgress = 1F;
    public float connectorProgress;
    public DyeColor ledColor = DyeColor.RED;
    public boolean showInnerParts;
    public boolean hasShell;
}

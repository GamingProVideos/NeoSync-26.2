package com.breakinblocks.neosync.api.shell;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A container that can store player's shell.
 */
public interface ShellStateContainer {
    /**
     * Attempts to retrieve a {@link ShellStateContainer} instance from a block in the world.
     */
    @Nullable
    static ShellStateContainer find(Level world, BlockPos pos) {
        if (!world.hasChunkAt(pos)) {
            return null;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof ShellStateContainer container ? container : null;
    }

    /**
     * Attempts to retrieve a {@link ShellStateContainer} that contains a given {@link ShellState}.
     */
    @Nullable
    static ShellStateContainer find(Level world, ShellState state) {
        ShellStateContainer container = find(world, state.getPos());
        if (container != null && container.getShellState() == state) {
            return container;
        }
        return null;
    }

    default boolean isRemotelyAccessible() {
        return true;
    }

    @Nullable
    ShellState getShellState();

    void setShellState(@Nullable ShellState state);

    @Nullable
    default DyeColor getColor() {
        ShellState state = this.getShellState();
        return state == null ? null : state.getColor();
    }
}

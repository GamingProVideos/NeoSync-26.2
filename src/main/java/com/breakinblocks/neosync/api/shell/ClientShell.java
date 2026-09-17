package com.breakinblocks.neosync.api.shell;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Client-side version of the {@link Shell}.
 */
public interface ClientShell extends Shell {
    @Override
    default boolean isClient() {
        return true;
    }

    /**
     * Begins an asynchronous sync operation.
     *
     * @param state Target state.
     * @return null if the sync process was started; otherwise, a failure reason is returned.
     */
    @Nullable PlayerSyncEvents.SyncFailureReason beginSync(ShellState state);

    /**
     * Handles the end of an asynchronous sync operation.
     *
     * @param startWorld Identifier of the world the sync operation was triggered in.
     * @param startPos Position the sync operation was triggered at.
     * @param startFacing Direction the player was looking at when the sync process started.
     * @param targetWorld Identifier of the target shell's world.
     * @param targetPos Position of the target shell.
     * @param targetFacing Direction the target shell is currently looking at.
     * @param storedState New state that was generated during the sync process, if any; otherwise, null.
     */
    void endSync(Identifier startWorld, BlockPos startPos, Direction startFacing, Identifier targetWorld, BlockPos targetPos, Direction targetFacing, @Nullable ShellState storedState);


    /**
     * @return Main player in the form of {@link ClientShell}.
     */
    static Optional<ClientShell> getMainPlayer() {
        return Optional.ofNullable((ClientShell)Minecraft.getInstance().player);
    }
}
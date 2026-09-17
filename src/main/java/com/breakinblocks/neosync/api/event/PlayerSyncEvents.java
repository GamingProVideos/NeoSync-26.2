package com.breakinblocks.neosync.api.event;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Events about the synchronization of {@linkplain Player players} with their shells.
 *
 * <p>These events can be categorized into two groups:
 * <ol>
 * <li>Simple listeners: {@link StartSyncing} and {@link StopSyncing}</li>
 * <li>Predicates: {@link AllowSyncing}, {@link AllowShellConstruction} and {@link AllowShellSelection}</li>
 * </ol>
 *
 * <p><b>Note:</b> Sync events are fired on both client side and server side.</li>
 */
public final class PlayerSyncEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Event invoker interface to maintain compatibility with Fabric-style event calls
     */
    public interface IEventInvoker {
        @Nullable
        SyncFailureReason allowSync(Player player, ShellState targetState);

        @Nullable
        ShellSelectionFailureReason allowShellSelection(Player player, ShellStateContainer targetContainer);

        @Nullable
        ShellConstructionFailureReason allowShellConstruction(Player player, ShellStateContainer targetContainer);

        void onStartSyncing(Player player, ShellState targetState);

        void onStopSyncing(Player player, BlockPos previousPos, @Nullable ShellState storedState);
    }

    /**
     * Event invoker implementation
     */
    private static class EventInvoker implements IEventInvoker {
        @Override
        public SyncFailureReason allowSync(Player player, ShellState targetState) {
            AllowSyncing event = new AllowSyncing(player, targetState);
            NeoForge.EVENT_BUS.post(event);
            return event.getFailureReason();
        }

        @Override
        public ShellSelectionFailureReason allowShellSelection(Player player, ShellStateContainer targetContainer) {
            AllowShellSelection event = new AllowShellSelection(player, targetContainer);
            NeoForge.EVENT_BUS.post(event);
            return event.getFailureReason();
        }

        @Override
        public ShellConstructionFailureReason allowShellConstruction(Player player, ShellStateContainer targetContainer) {
            AllowShellConstruction event = new AllowShellConstruction(player, targetContainer);
            NeoForge.EVENT_BUS.post(event);
            return event.getFailureReason();
        }

        @Override
        public void onStartSyncing(Player player, ShellState targetState) {
            postSafely(new StartSyncing(player, targetState));
        }

        @Override
        public void onStopSyncing(Player player, BlockPos previousPos, @Nullable ShellState storedState) {
            postSafely(new StopSyncing(player, previousPos, storedState));
        }

        private static void postSafely(PlayerSyncEvent event) {
            try {
                NeoForge.EVENT_BUS.post(event);
            } catch (Throwable t) {
                LOGGER.error("A listener of {} threw an exception", event.getClass().getSimpleName(), t);
            }
        }
    }

    /**
     * Event holder with invoker method for compatibility
     */
    public static class EventHolder {
        private final IEventInvoker invoker = new EventInvoker();

        public IEventInvoker invoker() {
            return invoker;
        }
    }

    public static final EventHolder ALLOW_SYNCING = new EventHolder();
    public static final EventHolder ALLOW_SHELL_SELECTION = new EventHolder();
    public static final EventHolder ALLOW_SHELL_CONSTRUCTION = new EventHolder();
    public static final EventHolder START_SYNCING = new EventHolder();
    public static final EventHolder STOP_SYNCING = new EventHolder();

    /**
     * Base class for all player sync events
     */
    public static class PlayerSyncEvent extends PlayerEvent {
        public PlayerSyncEvent(Player player) {
            super(player);
        }
    }

    /**
     * An event that checks whether a player can start syncing with the given shell.
     *
     * <p>If this event is canceled with a failure reason, it is used to fail the syncing process.
     * An uncanceled event means that the player will start syncing.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static class AllowSyncing extends PlayerSyncEvent implements ICancellableEvent {
        private final ShellState targetState;
        private SyncFailureReason failureReason = null;

        public AllowSyncing(Player player, ShellState targetState) {
            super(player);
            this.targetState = targetState;
        }

        public ShellState getTargetState() {
            return targetState;
        }

        public void setFailureReason(SyncFailureReason reason) {
            this.failureReason = reason;
            if (reason != null) {
                setCanceled(true);
            }
        }

        @Nullable
        public SyncFailureReason getFailureReason() {
            return failureReason;
        }
    }

    /**
     * An event that checks whether a player is able to create a new shell from their sample.
     *
     * <p>If this event is canceled with a failure reason, it is used to fail the construction process.
     * An uncanceled event means that a new shell will be created.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static class AllowShellConstruction extends PlayerSyncEvent implements ICancellableEvent {
        private final ShellStateContainer targetContainer;
        private ShellConstructionFailureReason failureReason = null;

        public AllowShellConstruction(Player player, ShellStateContainer targetContainer) {
            super(player);
            this.targetContainer = targetContainer;
        }

        public ShellStateContainer getTargetContainer() {
            return targetContainer;
        }

        public void setFailureReason(ShellConstructionFailureReason reason) {
            this.failureReason = reason;
            if (reason != null) {
                setCanceled(true);
            }
        }

        @Nullable
        public ShellConstructionFailureReason getFailureReason() {
            return failureReason;
        }
    }

    /**
     * An event that checks whether a player can select a shell to transfer their mind into.
     *
     * <p>If this event is canceled with a failure reason, it is used to fail the selection process.
     * An uncanceled event means that the player will be able to select a shell for the mind transfer process.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static class AllowShellSelection extends PlayerSyncEvent implements ICancellableEvent {
        private final ShellStateContainer targetContainer;
        private ShellSelectionFailureReason failureReason = null;

        public AllowShellSelection(Player player, ShellStateContainer targetContainer) {
            super(player);
            this.targetContainer = targetContainer;
        }

        public ShellStateContainer getTargetContainer() {
            return targetContainer;
        }

        public void setFailureReason(ShellSelectionFailureReason reason) {
            this.failureReason = reason;
            if (reason != null) {
                setCanceled(true);
            }
        }

        @Nullable
        public ShellSelectionFailureReason getFailureReason() {
            return failureReason;
        }
    }

    /**
     * An event that is called when a player starts to sync.
     */
    public static class StartSyncing extends PlayerSyncEvent {
        private final ShellState targetState;

        public StartSyncing(Player player, ShellState targetState) {
            super(player);
            this.targetState = targetState;
        }

        public ShellState getTargetState() {
            return targetState;
        }
    }

    /**
     * An event that is called when a player stops syncing and moves to another body.
     */
    public static class StopSyncing extends PlayerSyncEvent {
        private final BlockPos previousPos;
        @Nullable
        private final ShellState storedState;

        public StopSyncing(Player player, BlockPos previousPos, @Nullable ShellState storedState) {
            super(player);
            this.previousPos = previousPos;
            this.storedState = storedState;
        }

        public BlockPos getPreviousPos() {
            return previousPos;
        }

        @Nullable
        public ShellState getStoredState() {
            return storedState;
        }
    }

    // Failure reason interfaces
    @FunctionalInterface
    public interface SyncFailureReason {
        SyncFailureReason OTHER_PROBLEM = () -> null;
        SyncFailureReason INVALID_SHELL = create(Component.translatable("event.neosync.request.fail.invalid.shell"));
        SyncFailureReason INVALID_CURRENT_LOCATION = create(Component.translatable("event.neosync.request.fail.invalid.location.current"));
        SyncFailureReason INVALID_TARGET_LOCATION = create(Component.translatable("event.neosync.request.fail.invalid.location.target"));

        @Nullable
        Component toText();

        static SyncFailureReason create(@Nullable Component description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }

    @FunctionalInterface
    public interface ShellConstructionFailureReason {
        ShellConstructionFailureReason OTHER_PROBLEM = () -> null;
        ShellConstructionFailureReason OCCUPIED = create(Component.translatable("event.neosync.construction.fail.occupied"));
        ShellConstructionFailureReason NOT_ENOUGH_HEALTH = create(Component.translatable("event.neosync.construction.fail.health"));

        @Nullable
        Component toText();

        static ShellConstructionFailureReason create(@Nullable Component description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }

    @FunctionalInterface
    public interface ShellSelectionFailureReason {
        ShellSelectionFailureReason OTHER_PROBLEM = () -> null;

        @Nullable
        Component toText();

        static ShellSelectionFailureReason create(@Nullable Component description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }

    private PlayerSyncEvents() {
    }
}
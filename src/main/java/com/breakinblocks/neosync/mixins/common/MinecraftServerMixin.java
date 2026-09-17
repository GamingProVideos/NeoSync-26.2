package com.breakinblocks.neosync.mixins.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import com.mojang.datafixers.util.Pair;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateManager;
import com.breakinblocks.neosync.api.shell.ShellStateUpdateType;
import com.breakinblocks.neosync.common.utils.nbt.OfflinePlayerNbtManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ShellStateManager {
    @Shadow
    private PlayerList playerList;

    @Unique
    private final ConcurrentMap<UUID, ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>>> sync$pendingShellStates = new ConcurrentHashMap<>();

    @Override
    public void setAvailableShellStates(UUID owner, Stream<ShellState> states) {
        Shell shell = this.sync$getShellById(owner);
        if (shell != null) {
            shell.setAvailableShellStates(states);
        }
    }

    @Override
    public Stream<ShellState> getAvailableShellStates(UUID owner) {
        Shell shell = this.sync$getShellById(owner);
        return shell == null ? Stream.of() : shell.getAvailableShellStates();
    }

    @Override
    public ShellState getShellStateByUuid(UUID owner, UUID uuid) {
        Shell shell = this.sync$getShellById(owner);
        return shell == null ? null : shell.getShellStateByUuid(uuid);
    }

    @Override
    public void add(ShellState state) {
        Shell shell = this.sync$getShellByItsState(state);
        if (shell != null) {
            shell.add(state);
        }
    }

    @Override
    public void remove(ShellState state) {
        Shell shell = this.sync$getShellByItsState(state);
        if (shell == null) {
            this.sync$putPendingUpdate(state, ShellStateUpdateType.REMOVE);
        } else {
            shell.remove(state);
        }
    }

    @Override
    public void update(ShellState state) {
        Shell shell = this.sync$getShellByItsState(state);
        if (shell == null) {
            this.sync$putPendingUpdate(state, ShellStateUpdateType.UPDATE);
        } else {
            shell.update(state);
        }
    }

    @Override
    public Collection<Pair<ShellStateUpdateType, ShellState>> peekPendingUpdates(UUID owner) {
        Map<UUID, Pair<ShellStateUpdateType, ShellState>> shells = this.sync$pendingShellStates.get(owner);
        if (shells == null) {
            return List.of();
        }
        return shells.values();
    }

    @Override
    public void clearPendingUpdates(UUID owner) {
        this.sync$pendingShellStates.remove(owner);
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;removeAll()V"))
    private void sync$onShutdown(CallbackInfo ci) {
        for (Map.Entry<UUID, ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>>> entry : this.sync$pendingShellStates.entrySet()) {
            UUID userId = entry.getKey();
            Collection<Pair<ShellStateUpdateType, ShellState>> updates = entry.getValue().values();
            if (updates.size() == 0) {
                continue;
            }

            OfflinePlayerNbtManager.editPlayerNbt((MinecraftServer)(Object)this, userId, nbt -> {
                ListTag list = nbt.getList("Shells").orElse(new ListTag());
                Map<UUID, ShellState> shells = list.stream()
                        .filter(t -> t instanceof CompoundTag)
                        .map(t -> ShellState.fromNbt((CompoundTag) t))
                        .collect(Collectors.toMap(ShellState::getUuid, x -> x));

                for (Pair<ShellStateUpdateType, ShellState> update : updates) {
                    ShellState state = update.getSecond();
                    switch (update.getFirst()) {
                        case ADD, UPDATE -> {
                            if (userId.equals(state.getOwnerUuid())) {
                                shells.put(state.getUuid(), state);
                            }
                        }
                        case REMOVE -> shells.remove(state.getUuid());
                    }
                }

                ListTag shellList = new ListTag();
                shells.values().stream().map(x -> x.writeNbt(new CompoundTag())).forEach(shellList::add);
                nbt.put("Shells", shellList);
            });
        }
        this.sync$pendingShellStates.clear();
    }

    @Unique
    private void sync$putPendingUpdate(ShellState state, ShellStateUpdateType updateType) {
        if (state == null || updateType == ShellStateUpdateType.NONE) {
            return;
        }

        ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>> updates = this.sync$pendingShellStates.get(state.getOwnerUuid());
        if (updates == null) {
            updates = new ConcurrentHashMap<>();
            this.sync$pendingShellStates.put(state.getOwnerUuid(), updates);
        }
        updates.put(state.getUuid(), Pair.of(updateType, state));
    }

    @Unique
    private Shell sync$getShellById(UUID id) {
        return this.isValidShellOwnerUuid(id) ? (Shell)this.playerList.getPlayer(id) : null;
    }

    @Unique
    private Shell sync$getShellByItsState(ShellState state) {
        return this.isValidShellState(state) ? (Shell)this.playerList.getPlayer(state.getOwnerUuid()) : null;
    }
}
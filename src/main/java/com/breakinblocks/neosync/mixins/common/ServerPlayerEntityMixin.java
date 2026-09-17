package com.breakinblocks.neosync.mixins.common;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;

import com.breakinblocks.neosync.api.SyncTeleport;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.networking.PlayerIsAlivePacket;
import com.breakinblocks.neosync.api.networking.ShellStateUpdatePacket;
import com.breakinblocks.neosync.api.networking.ShellUpdatePacket;
import com.breakinblocks.neosync.common.config.SyncConfig;
import com.breakinblocks.neosync.api.networking.SynchronizationResponsePacket;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ServerShell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.api.shell.ShellStateManager;
import com.breakinblocks.neosync.api.shell.ShellStateUpdateType;
import com.breakinblocks.neosync.common.entity.KillableEntity;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerEntityMixin extends Player implements ServerShell, KillableEntity {
    @Shadow
    private int lastSentExp;

    @Shadow
    private float lastSentHealth;

    @Shadow
    private int lastSentFood;

    @Shadow @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    protected abstract void removeEntitiesOnShoulder();

    private ServerPlayerEntityMixin(Level level, GameProfile profile) {
        super(level, profile);
    }

    @Unique
    private static final Logger SYNC_LOGGER = LogUtils.getLogger();

    @Unique
    private boolean isArtificial = false;

    @Unique
    private boolean shellDirty = false;

    @Unique
    private boolean undead = false;

    @Unique
    private UUID pendingSyncTarget = null;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();

    @Unique
    private Map<UUID, Pair<ShellStateUpdateType, ShellState>> shellStateChanges = new ConcurrentHashMap<>();

    @Shadow
    protected abstract void tellNeutralMobsThatIDied();

    @Override
    public UUID getShellOwnerUuid() {
        return ((ServerPlayer) (Object) this).getGameProfile().id();
    }

    @Override
    public boolean isArtificial() {
        return this.isArtificial;
    }

    @Override
    public void changeArtificialStatus(boolean isArtificial) {
        if (this.isArtificial != isArtificial) {
            this.isArtificial = isArtificial;
            this.shellDirty = true;
        }
    }

    @Override
    public Either<ShellState, PlayerSyncEvents.SyncFailureReason> sync(ShellState state) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        BlockPos currentPos = player.blockPosition();
        Level currentWorld = player.level();

        if (this.pendingSyncTarget != null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        if (!this.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_SHELL);
        }

        UUID requestedUuid = state.getUuid();
        boolean isDead = player.isDeadOrDying();
        ShellStateContainer currentShellContainer = isDead ? null : ShellStateContainer.find(currentWorld, currentPos);
        if (currentShellContainer != null && currentShellContainer.getShellState() != null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
        }

        PlayerSyncEvents.ShellSelectionFailureReason selectionFailureReason = PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, currentShellContainer);
        if (selectionFailureReason != null) {
            return Either.right(selectionFailureReason::toText);
        }

        Identifier targetWorldId = state.getWorld();
        ServerLevel targetWorld = WorldUtil.findWorld(this.server.getAllLevels(), targetWorldId).orElse(null);
        if (targetWorld == null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        ShellStateContainer targetShellContainer = this.findTargetContainer(targetWorld, state);
        if (!state.isVirtual()) {
            if (targetShellContainer == null) {
                return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
            }
            state = targetShellContainer.getShellState();
        }
        PlayerSyncEvents.SyncFailureReason finalFailureReason = this.canBeApplied(state) ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(this, state) : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;
        if (finalFailureReason != null) {
            return Either.right(finalFailureReason);
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(this, state);

        if (isDead && !this.undead) {
            this.pendingSyncTarget = requestedUuid;
            sendEmptyDeathMessageInChat(player);
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        if (!isDead && currentShellContainer == null) {
            this.pendingSyncTarget = requestedUuid;
            player.hurtServer(player.level(), player.damageSources().genericKill(), Float.MAX_VALUE);
            if (!player.isDeadOrDying()) {
                this.pendingSyncTarget = null;
                return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
            }
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        ShellState storedState = null;
        if (currentShellContainer != null) {
            storedState = ShellState.of(player, currentPos, currentShellContainer.getColor());
            currentShellContainer.setShellState(storedState);
            if (currentShellContainer.isRemotelyAccessible()) {
                this.add(storedState);
            }
        }

        if (!this.moveInto(state, targetShellContainer)) {
            if (currentShellContainer != null) {
                if (storedState != null && currentShellContainer.isRemotelyAccessible()) {
                    this.remove(storedState);
                }
                currentShellContainer.setShellState(null);
            }
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, currentPos, storedState);
        return Either.left(storedState);
    }

    @Override
    public UUID getPendingSyncTarget() {
        return this.pendingSyncTarget;
    }

    @Unique
    private ShellStateContainer findTargetContainer(ServerLevel targetWorld, ShellState state) {
        if (state.isVirtual()) {
            return null;
        }

        BlockPos targetPos = state.getPos();
        LevelChunk targetChunk = targetWorld.getChunk(targetPos.getX() >> 4, targetPos.getZ() >> 4);
        return targetChunk == null ? null : ShellStateContainer.find(targetWorld, targetPos);
    }

    @Unique
    private boolean moveInto(ShellState state, ShellStateContainer targetShellContainer) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (targetShellContainer == null) {
            if (!this.tryApply(ShellState.anchor(player, state.getWorld(), state.getPos()))) {
                return false;
            }
            if (state.isTemporary()) {
                this.remove(state);
            }
            return true;
        }
        if (!this.tryApply(state)) {
            return false;
        }
        targetShellContainer.setShellState(null);
        this.remove(state);
        return true;
    }

    @Unique
    private void completePendingSync() {
        ServerPlayer player = (ServerPlayer)(Object)this;
        ShellState state = this.shellsById.get(this.pendingSyncTarget);
        this.pendingSyncTarget = null;

        BlockPos previousPos = player.blockPosition();
        Identifier previousWorldId = WorldUtil.getId(player.level());
        Direction previousFacing = BlockPosUtil.getHorizontalFacing(previousPos, player.level()).orElse(player.getDirection().getOpposite());

        ServerLevel targetWorld = state == null ? null : WorldUtil.findWorld(this.server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            this.failPendingSync(previousWorldId, previousPos, previousFacing);
            return;
        }

        ShellStateContainer targetShellContainer = this.findTargetContainer(targetWorld, state);
        if (!state.isVirtual()) {
            if (targetShellContainer == null || !this.canBeApplied(targetShellContainer.getShellState())) {
                this.failPendingSync(previousWorldId, previousPos, previousFacing);
                return;
            }
            state = targetShellContainer.getShellState();
        }

        if (!this.moveInto(state, targetShellContainer)) {
            this.failPendingSync(previousWorldId, previousPos, previousFacing);
            return;
        }

        new SynchronizationResponsePacket(
                previousWorldId, previousPos, previousFacing,
                WorldUtil.getId(player.level()), player.blockPosition(), player.getDirection().getOpposite(),
                Optional.empty()).send(player);

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, previousPos, null);
    }

    @Unique
    private void failPendingSync(Identifier worldId, BlockPos pos, Direction facing) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        player.sendSystemMessage(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION.toText());
        new SynchronizationResponsePacket(worldId, pos, facing, worldId, pos, facing, Optional.empty()).send(player);
    }

    @Override
    public void apply(ShellState state) {
        this.tryApply(state);
    }

    @Unique
    private boolean tryApply(ShellState state) {
        Objects.requireNonNull(state);

        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        MinecraftServer server = Objects.requireNonNull(serverPlayer.level().getServer());
        ServerLevel targetWorld = WorldUtil.findWorld(server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            SYNC_LOGGER.warn("Sync target world {} does not exist; leaving {} untouched", state.getWorld(), serverPlayer.getName().getString());
            return false;
        }

        this.stopRiding();
        this.removeEntitiesOnShoulder();
        this.clearFire();
        this.setTicksFrozen(0);
        this.setRemainingFireTicks(0);
        this.removeAllEffects();

        new PlayerIsAlivePacket(serverPlayer).sendToAll(server);
        SyncTeleport.begin();
        try {
            if (!this.teleport(targetWorld, state)) {
                SYNC_LOGGER.warn("Sync teleport to {} was refused; leaving {} untouched", state.getWorld(), serverPlayer.getName().getString());
                return false;
            }
        } finally {
            SyncTeleport.end();
        }
        this.isArtificial = state.isArtificial();

        Inventory inventory = serverPlayer.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        state.getInventory().copyTo(inventory);
        inventory.setSelectedSlot(selectedSlot);

        ShellStateComponent playerComponent = ShellStateComponent.of(serverPlayer);
        playerComponent.clone(state.getComponent());

        serverPlayer.setGameMode(GameType.byId(state.getGameMode()));
        serverPlayer.setHealth(state.getHealth());
        serverPlayer.experienceLevel = state.getExperienceLevel();
        serverPlayer.experienceProgress = state.getExperienceProgress();
        serverPlayer.totalExperience = state.getTotalExperience();
        serverPlayer.getFoodData().setFoodLevel(state.getFoodLevel());
        serverPlayer.getFoodData().setSaturation(state.getSaturationLevel());

        this.undead = false;
        this.dead = false;
        this.deathTime = 0;
        this.fallDistance = 0;
        this.lastSentExp = -1;
        this.lastSentHealth = -1;
        this.lastSentFood = -1;
        this.shellDirty = true;
        return true;
    }

    @Override
    public Stream<ShellState> getAvailableShellStates() {
        return this.shellsById.values().stream();
    }

    @Override
    public void setAvailableShellStates(Stream<ShellState> states) {
        this.shellsById = states.collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellDirty = true;
    }

    @Override
    public ShellState getShellStateByUuid(UUID uuid) {
        return uuid == null ? null : this.shellsById.get(uuid);
    }

    @Override
    public void add(ShellState state) {
        if (!this.canBeApplied(state)) {
            return;
        }
        this.shellsById.put(state.getUuid(), state);
        this.shellStateChanges.put(state.getUuid(), Pair.of(ShellStateUpdateType.ADD, state));
    }

    @Override
    public boolean isDeathSyncEnabled() {
        return SyncConfig.getInstance().autoSyncOnDeath();
    }

    @Override
    public void setDeathSyncEnabled(boolean enabled) {
    }

    @Override
    public void remove(ShellState state) {
        if (state == null) return;
        if (this.shellsById.remove(state.getUuid()) != null) {
            this.shellStateChanges.put(state.getUuid(), Pair.of(ShellStateUpdateType.REMOVE, state));
        }
    }

    @Override
    public void update(ShellState state) {
        if (state == null) return;
        boolean updated;
        if (this.canBeApplied(state)) {
            updated = this.shellsById.put(state.getUuid(), state) != null;
        } else {
            updated = this.shellsById.computeIfPresent(state.getUuid(), (a, b) -> state) != null;
        }
        this.shellStateChanges.put(state.getUuid(), Pair.of(updated ? ShellStateUpdateType.UPDATE : ShellStateUpdateType.ADD, state));
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void playerTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;

        if (this.pendingSyncTarget != null && !player.isRemoved()) {
            this.completePendingSync();
        }

        if (this.shellDirty) {
            this.shellDirty = false;
            this.shellStateChanges.clear();
            new ShellUpdatePacket(WorldUtil.getId(player.level()), this.isArtificial, SyncConfig.getInstance().autoSyncOnDeath(), this.shellsById.values()).send(player);
        }

        for (Pair<ShellStateUpdateType, ShellState> upd : this.shellStateChanges.values()) {
            new ShellStateUpdatePacket(upd.getFirst(), upd.getSecond()).send(player);
        }
        this.shellStateChanges.clear();
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object hidePendingSyncDeathMessage(GameRules rules, GameRule<?> rule, Operation<Object> original) {
        if (this.pendingSyncTarget != null && rule == GameRules.SHOW_DEATH_MESSAGES) {
            return Boolean.FALSE;
        }
        return original.call(rules, rule);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (!this.isArtificial || this.pendingSyncTarget != null) {
            return;
        }

        ServerPlayer player = (ServerPlayer)(Object)this;
        if (!this.hasAutoSyncTarget()) {
            return;
        }

        this.gameEvent(GameEvent.ENTITY_DIE);
        if (CommonHooks.onLivingDeath(player, source)) {
            ci.cancel();
            return;
        }

        if (player.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
            sendDeathMessageInChat(player, source);
        } else {
            sendEmptyDeathMessageInChat(player);
        }

        this.removeEntitiesOnShoulder();
        if (player.level().getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(player.level(), source);
            this.destroyVanishingCursedItems();
            player.getInventory().dropAll();
        }

        this.undead = true;
        ci.cancel();
    }

    @Override
    public boolean updateKillableEntityPostDeath() {
        ServerPlayer player = (ServerPlayer)(Object)this;
        player.deathTime = Mth.clamp(++player.deathTime, 0, 20);
        if (this.pendingSyncTarget != null) {
            return true;
        }

        if (this.isArtificial && this.hasAutoSyncTarget()) {
            return true;
        }

        if (this.undead) {
            player.die(player.level().damageSources().magic());
            this.undead = false;
        }

        if (player.deathTime == 20) {
            player.level().broadcastEntityEvent(player, (byte)60);
            player.remove(Entity.RemovalReason.KILLED);
        }
        return true;
    }

    @Unique
    private static void sendEmptyDeathMessageInChat(ServerPlayer player) {
        player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), Component.empty()));
    }

    @Unique
    private static void sendDeathMessageInChat(ServerPlayer player, DamageSource source) {
        Component text = player.getCombatTracker().getDeathMessage();
        player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), text));
        Team team = player.getTeam();
        if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
            if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                player.level().getServer().getPlayerList().broadcastSystemToTeam(player, text);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                player.level().getServer().getPlayerList().broadcastSystemToAllExceptTeam(player, text);
            }
        } else {
            player.level().getServer().getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbt(ValueOutput out, CallbackInfo ci) {
        out.putBoolean("IsArtificial", this.isArtificial);
        ValueOutput.TypedOutputList<CompoundTag> list = out.list("Shells", CompoundTag.CODEC);
        this.shellsById.values().forEach(s -> list.add(s.writeNbt(new CompoundTag())));
        if (this.pendingSyncTarget != null) {
            out.store("PendingSyncTarget", UUIDUtil.CODEC, this.pendingSyncTarget);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbt(ValueInput in, CallbackInfo ci) {
        this.isArtificial = in.getBooleanOr("IsArtificial", false);
        this.pendingSyncTarget = in.read("PendingSyncTarget", UUIDUtil.CODEC).orElse(null);
        ValueInput.TypedInputList<CompoundTag> list = in.listOrEmpty("Shells", CompoundTag.CODEC);
        this.shellsById = list.stream()
                .map(ShellState::fromNbt)
                .collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));

        Collection<Pair<ShellStateUpdateType, ShellState>> updates = ((ShellStateManager)this.server).popPendingUpdates(this.getShellOwnerUuid());
        for (Pair<ShellStateUpdateType, ShellState> update : updates) {
            ShellState state = update.getSecond();
            switch (update.getFirst()) {
                case ADD, UPDATE -> {
                    if (this.getShellOwnerUuid().equals(state.getOwnerUuid())) {
                        this.shellsById.put(state.getUuid(), state);
                    }
                }
                case REMOVE -> this.shellsById.remove(state.getUuid());
            }
        }

        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        Shell shell = (Shell)oldPlayer;
        this.isArtificial = alive && shell.isArtificial();
        this.pendingSyncTarget = ((ServerShell)oldPlayer).getPendingSyncTarget();
        this.shellsById = shell.getAvailableShellStates().collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    private void setWorld(ServerLevel world, CallbackInfo ci) {
        if (world != ((ServerPlayer)(Object)this).level()) {
            this.shellDirty = true;
        }
    }

    @Unique
    private boolean teleport(ServerLevel targetWorld, ShellState state) {
        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        BlockPos pos = state.getPos();
        LevelChunk chunk = targetWorld.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        float yaw = BlockPosUtil.getHorizontalFacing(pos, chunk).map(d -> d.getOpposite().toYRot()).orElse(0F);

        boolean moved = serverPlayer.teleportTo(targetWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), yaw, 0F, true);
        if (!moved || serverPlayer.level() != targetWorld) {
            return false;
        }

        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
        serverPlayer.fallDistance = 0F;
        return true;
    }
}

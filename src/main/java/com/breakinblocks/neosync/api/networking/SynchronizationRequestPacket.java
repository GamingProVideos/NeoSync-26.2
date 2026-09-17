package com.breakinblocks.neosync.api.networking;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.shell.ServerShell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Optional;
import java.util.UUID;

public record SynchronizationRequestPacket(Optional<UUID> shellUuid) implements CustomPacketPayload {
    public static final Type<SynchronizationRequestPacket> TYPE = new Type<>(NeoSync.locate("shell/synchronization/request"));

    public static final StreamCodec<FriendlyByteBuf, SynchronizationRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SynchronizationRequestPacket::shellUuid,
            SynchronizationRequestPacket::new
    );

    public SynchronizationRequestPacket(ShellState shell) {
        this(shell == null ? Optional.empty() : Optional.of(shell.getUuid()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send() {
        ClientPacketDistributor.sendToServer(this);
    }

    public static void handle(SynchronizationRequestPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !(player instanceof ServerShell shell)) {
                return;
            }
            ShellState state = payload.shellUuid.map(shell::getShellStateByUuid).orElse(null);

            BlockPos currentPos = player.blockPosition();
            Level currentWorld = player.level();
            Identifier currentWorldId = WorldUtil.getId(currentWorld);
            Direction currentFacing = BlockPosUtil.getHorizontalFacing(currentPos, currentWorld)
                    .orElse(player.getDirection().getOpposite());

            Either<ShellState, PlayerSyncEvents.SyncFailureReason> result = shell.sync(state);
            if (shell.getPendingSyncTarget() != null) {
                return;
            }

            result.ifLeft(storedState -> {
                if (state == null) {
                    return;
                }
                Identifier targetWorldId = state.getWorld();
                BlockPos targetPos = state.getPos();
                Direction targetFacing = player.getDirection().getOpposite();
                PacketDistributor.sendToPlayer(player, new SynchronizationResponsePacket(
                        currentWorldId, currentPos, currentFacing,
                        targetWorldId, targetPos, targetFacing,
                        Optional.ofNullable(storedState)));
            }).ifRight(failureReason -> {
                Component failureText = failureReason.toText();
                if (failureText != null) {
                    player.sendSystemMessage(failureText);
                }
                PacketDistributor.sendToPlayer(player, new SynchronizationResponsePacket(
                        currentWorldId, currentPos, currentFacing,
                        currentWorldId, currentPos, currentFacing,
                        Optional.empty()));
            });
        });
    }
}

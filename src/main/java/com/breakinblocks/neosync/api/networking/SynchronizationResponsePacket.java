package com.breakinblocks.neosync.api.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.shell.ShellState;

import java.util.Optional;

public record SynchronizationResponsePacket(
        Identifier startWorld,
        BlockPos startPos,
        Direction startFacing,
        Identifier targetWorld,
        BlockPos targetPos,
        Direction targetFacing,
        Optional<ShellState> storedState
) implements CustomPacketPayload {

    public static final Type<SynchronizationResponsePacket> TYPE = new Type<>(NeoSync.locate("shell/synchronization/response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SynchronizationResponsePacket> STREAM_CODEC = StreamCodec.of(
            SynchronizationResponsePacket::encode,
            SynchronizationResponsePacket::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    private static void encode(RegistryFriendlyByteBuf buf, SynchronizationResponsePacket payload) {
        buf.writeIdentifier(payload.startWorld);
        buf.writeBlockPos(payload.startPos);
        buf.writeVarInt(payload.startFacing.get3DDataValue());
        buf.writeIdentifier(payload.targetWorld);
        buf.writeBlockPos(payload.targetPos);
        buf.writeVarInt(payload.targetFacing.get3DDataValue());
        ByteBufCodecs.optional(ShellState.STREAM_CODEC).encode(buf, payload.storedState);
    }

    private static SynchronizationResponsePacket decode(RegistryFriendlyByteBuf buf) {
        Identifier startWorld = buf.readIdentifier();
        BlockPos startPos = buf.readBlockPos();
        Direction startFacing = Direction.from3DDataValue(buf.readVarInt());
        Identifier targetWorld = buf.readIdentifier();
        BlockPos targetPos = buf.readBlockPos();
        Direction targetFacing = Direction.from3DDataValue(buf.readVarInt());
        Optional<ShellState> storedState = ByteBufCodecs.optional(ShellState.STREAM_CODEC).decode(buf);
        return new SynchronizationResponsePacket(startWorld, startPos, startFacing, targetWorld, targetPos, targetFacing, storedState);
    }

    public static void handle(SynchronizationResponsePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onSynchronizationResponse(payload));
    }
}

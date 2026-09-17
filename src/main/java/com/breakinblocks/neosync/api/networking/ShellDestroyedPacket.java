package com.breakinblocks.neosync.api.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;

public record ShellDestroyedPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ShellDestroyedPacket> TYPE = new Type<>(NeoSync.locate("shell/destroyed"));

    public static final StreamCodec<FriendlyByteBuf, ShellDestroyedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShellDestroyedPacket::pos,
            ShellDestroyedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerLevel world, BlockPos pos, double radius) {
        PacketDistributor.sendToPlayersNear(world, null, pos.getX(), pos.getY(), pos.getZ(), radius, this);
    }

    public static void handle(ShellDestroyedPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onShellDestroyed(payload));
    }
}

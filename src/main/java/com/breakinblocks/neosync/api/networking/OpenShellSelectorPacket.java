package com.breakinblocks.neosync.api.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;

public record OpenShellSelectorPacket() implements CustomPacketPayload {
    public static final Type<OpenShellSelectorPacket> TYPE = new Type<>(NeoSync.locate("shell/open_selector"));

    public static final StreamCodec<FriendlyByteBuf, OpenShellSelectorPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenShellSelectorPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public static void handle(OpenShellSelectorPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onOpenShellSelector());
    }
}

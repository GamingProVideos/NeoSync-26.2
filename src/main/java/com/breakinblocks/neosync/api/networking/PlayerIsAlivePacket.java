package com.breakinblocks.neosync.api.networking;

import net.minecraft.util.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;

import java.util.UUID;

public record PlayerIsAlivePacket(UUID playerUuid) implements CustomPacketPayload {
    public static final Type<PlayerIsAlivePacket> TYPE = new Type<>(NeoSync.locate("shell/alive"));

    public static final StreamCodec<FriendlyByteBuf, PlayerIsAlivePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerIsAlivePacket::playerUuid,
            PlayerIsAlivePacket::new
    );

    public PlayerIsAlivePacket {
        if (playerUuid == null) {
            playerUuid = Util.NIL_UUID;
        }
    }

    public PlayerIsAlivePacket(Player player) {
        this(player == null ? Util.NIL_UUID : player.getUUID());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public void sendToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, this);
        }
    }

    public static void handle(PlayerIsAlivePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onPlayerIsAlive(payload));
    }
}

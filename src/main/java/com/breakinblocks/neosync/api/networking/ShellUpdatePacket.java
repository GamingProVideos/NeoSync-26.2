package com.breakinblocks.neosync.api.networking;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record ShellUpdatePacket(Identifier worldId, boolean isArtificial, boolean autoSyncOnDeath, List<ShellState> states) implements CustomPacketPayload {
    public static final Type<ShellUpdatePacket> TYPE = new Type<>(NeoSync.locate("shell/update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShellUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ShellUpdatePacket::worldId,
            ByteBufCodecs.BOOL, ShellUpdatePacket::isArtificial,
            ByteBufCodecs.BOOL, ShellUpdatePacket::autoSyncOnDeath,
            ShellState.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ShellUpdatePacket::states,
            ShellUpdatePacket::new
    );

    public ShellUpdatePacket(Identifier worldId, boolean isArtificial, boolean autoSyncOnDeath, Collection<ShellState> states) {
        this(worldId, isArtificial, autoSyncOnDeath, states == null ? List.of() : List.copyOf(states));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public static void handle(ShellUpdatePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onShellUpdate(payload));
    }
}

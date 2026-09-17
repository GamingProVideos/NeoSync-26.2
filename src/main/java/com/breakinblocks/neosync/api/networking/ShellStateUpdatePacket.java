package com.breakinblocks.neosync.api.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateUpdateType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ShellStateUpdatePacket(
        ShellStateUpdateType kind,
        @Nullable ShellState addedState,
        @Nullable UUID targetUuid,
        float progress,
        @Nullable DyeColor color,
        @Nullable BlockPos pos,
        @Nullable String name,
        boolean manualOnly
) implements CustomPacketPayload {

    public static final Type<ShellStateUpdatePacket> TYPE = new Type<>(NeoSync.locate("shell/state/update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShellStateUpdatePacket> STREAM_CODEC = StreamCodec.of(
            ShellStateUpdatePacket::encode,
            ShellStateUpdatePacket::decode
    );

    public ShellStateUpdatePacket(ShellStateUpdateType kind, ShellState state) {
        this(kind, adoptedState(kind, state), state == null ? null : state.getUuid(),
             state == null ? 0F : state.getProgress(),
             state == null ? null : state.getColor(),
             state == null ? null : state.getPos(),
             state == null ? null : state.getName(),
             state != null && state.isManualOnly());
        if (state == null && kind != ShellStateUpdateType.NONE) {
            throw new IllegalStateException("ShellStateUpdatePacket requires a non-null state for kind " + kind);
        }
    }

    private static @Nullable ShellState adoptedState(ShellStateUpdateType kind, ShellState state) {
        return kind == ShellStateUpdateType.ADD ? state : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    private static void encode(RegistryFriendlyByteBuf buf, ShellStateUpdatePacket payload) {
        buf.writeEnum(payload.kind);
        switch (payload.kind) {
            case ADD -> {
                if (payload.addedState == null) {
                    throw new IllegalStateException("ADD packet requires non-null addedState");
                }
                ShellState.STREAM_CODEC.encode(buf, payload.addedState);
            }
            case REMOVE -> {
                if (payload.targetUuid == null) {
                    throw new IllegalStateException("REMOVE packet requires non-null targetUuid");
                }
                buf.writeUUID(payload.targetUuid);
            }
            case UPDATE -> {
                if (payload.targetUuid == null || payload.pos == null) {
                    throw new IllegalStateException("UPDATE packet requires non-null targetUuid and pos");
                }
                buf.writeUUID(payload.targetUuid);
                buf.writeVarInt((int) (payload.progress * 100));
                buf.writeVarInt(payload.color == null ? Byte.MAX_VALUE : payload.color.getId());
                buf.writeBlockPos(payload.pos);
                buf.writeUtf(payload.name == null ? "" : payload.name, ShellState.MAX_NAME_LENGTH);
                buf.writeBoolean(payload.manualOnly);
            }
            case NONE -> { }
        }
    }

    private static ShellStateUpdatePacket decode(RegistryFriendlyByteBuf buf) {
        ShellStateUpdateType kind = buf.readEnum(ShellStateUpdateType.class);
        return switch (kind) {
            case ADD -> new ShellStateUpdatePacket(kind, ShellState.STREAM_CODEC.decode(buf), null, 0F, null, null, null, false);
            case REMOVE -> new ShellStateUpdatePacket(kind, null, buf.readUUID(), 0F, null, null, null, false);
            case UPDATE -> {
                UUID uuid = buf.readUUID();
                float progress = Mth.clamp(buf.readVarInt() / 100F, 0F, 1F);
                int colorId = buf.readVarInt();
                DyeColor color = colorId < 0 || colorId > 15 ? null : DyeColor.byId(colorId);
                BlockPos pos = buf.readBlockPos();
                String name = buf.readUtf(ShellState.MAX_NAME_LENGTH);
                boolean manualOnly = buf.readBoolean();
                yield new ShellStateUpdatePacket(kind, null, uuid, progress, color, pos, name.isEmpty() ? null : name, manualOnly);
            }
            case NONE -> new ShellStateUpdatePacket(kind, null, null, 0F, null, null, null, false);
        };
    }

    public static void handle(ShellStateUpdatePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworkHandler.onShellStateUpdate(payload));
    }
}

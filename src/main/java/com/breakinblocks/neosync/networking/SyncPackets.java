package com.breakinblocks.neosync.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.networking.OpenShellSelectorPacket;
import com.breakinblocks.neosync.api.networking.PlayerIsAlivePacket;
import com.breakinblocks.neosync.api.networking.ShellDestroyedPacket;
import com.breakinblocks.neosync.api.networking.ShellRenamePacket;
import com.breakinblocks.neosync.api.networking.ShellStateUpdatePacket;
import com.breakinblocks.neosync.api.networking.ShellUpdatePacket;
import com.breakinblocks.neosync.api.networking.SynchronizationRequestPacket;
import com.breakinblocks.neosync.api.networking.SynchronizationResponsePacket;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class SyncPackets {
    private static final String PROTOCOL_VERSION = "1";

    private SyncPackets() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NeoSync.MOD_ID).versioned(PROTOCOL_VERSION);

        registrar.playToServer(
                SynchronizationRequestPacket.TYPE,
                SynchronizationRequestPacket.STREAM_CODEC,
                SynchronizationRequestPacket::handle);

        registrar.playToServer(
                ShellRenamePacket.TYPE,
                ShellRenamePacket.STREAM_CODEC,
                ShellRenamePacket::handle);

        registrar.playToClient(
                SynchronizationResponsePacket.TYPE,
                SynchronizationResponsePacket.STREAM_CODEC,
                SynchronizationResponsePacket::handle);

        registrar.playToClient(
                ShellUpdatePacket.TYPE,
                ShellUpdatePacket.STREAM_CODEC,
                ShellUpdatePacket::handle);

        registrar.playToClient(
                ShellStateUpdatePacket.TYPE,
                ShellStateUpdatePacket.STREAM_CODEC,
                ShellStateUpdatePacket::handle);

        registrar.playToClient(
                PlayerIsAlivePacket.TYPE,
                PlayerIsAlivePacket.STREAM_CODEC,
                PlayerIsAlivePacket::handle);

        registrar.playToClient(
                OpenShellSelectorPacket.TYPE,
                OpenShellSelectorPacket.STREAM_CODEC,
                OpenShellSelectorPacket::handle);

        registrar.playToClient(
                ShellDestroyedPacket.TYPE,
                ShellDestroyedPacket.STREAM_CODEC,
                ShellDestroyedPacket::handle);
    }
}

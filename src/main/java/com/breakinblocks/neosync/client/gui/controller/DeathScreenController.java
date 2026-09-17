package com.breakinblocks.neosync.client.gui.controller;

import com.breakinblocks.neosync.NeoSync;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = NeoSync.MOD_ID, value = Dist.CLIENT)
public final class DeathScreenController {
    private static volatile boolean suspended;

    private DeathScreenController() {}

    public static boolean isSuspended() {
        return suspended;
    }

    public static void suspend() {
        suspended = true;
    }

    public static void restore() {
        suspended = false;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        restore();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        restore();
    }

    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        restore();
    }
}

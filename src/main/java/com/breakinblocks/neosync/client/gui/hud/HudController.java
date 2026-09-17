package com.breakinblocks.neosync.client.gui.hud;

import com.breakinblocks.neosync.NeoSync;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = NeoSync.MOD_ID, value = Dist.CLIENT)
public final class HudController {
    private static Boolean wasHudHidden;

    private HudController() {}

    public static void show() {
        setHudHidden(false);
    }

    public static void hide() {
        setHudHidden(true);
    }

    public static void restore() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.hud == null) {
            wasHudHidden = null;
            return;
        }
        if (wasHudHidden != null) {
            setHudHiddenState(mc, wasHudHidden);
            wasHudHidden = null;
        }
    }

    private static void setHudHidden(boolean value) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.hud == null) {
            return;
        }
        if (wasHudHidden == null) {
            wasHudHidden = mc.gui.hud.isHidden();
        }
        setHudHiddenState(mc, value);
    }

    private static void setHudHiddenState(Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden) {
            mc.gui.hud.toggle();
        }
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

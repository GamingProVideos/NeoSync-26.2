package com.breakinblocks.neosync.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;

public final class ClientRegistryHelper {
    private ClientRegistryHelper() {}

    public static HolderLookup.Provider tryProvider() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.level != null) {
                return mc.level.registryAccess();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}

package com.breakinblocks.neosync.compat.mekanism;

import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class MekanismCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean LOADED = ModList.get().isLoaded("mekanism");

    private MekanismCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static void init() {
        if (!LOADED) {
            return;
        }

        try {
            ShellStateComponentFactoryRegistry.getInstance().register(new MekanismShellStateComponentFactory());
        } catch (Throwable t) {
            LOGGER.warn("Mekanism is loaded but the shell state integration failed to initialize", t);
        }
    }
}

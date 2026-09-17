package com.breakinblocks.neosync.compat.curios;

import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class CuriosCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean LOADED = ModList.get().isLoaded("curios");

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static void init() {
        if (!LOADED) {
            return;
        }

        try {
            Class<?> cls = Class.forName("com.breakinblocks.neosync.compat.curios.CuriosShellStateComponentFactory");
            ShellStateComponentFactoryRegistry.ShellStateComponentFactory factory =
                    (ShellStateComponentFactoryRegistry.ShellStateComponentFactory)cls.getDeclaredConstructor().newInstance();
            ShellStateComponentFactoryRegistry.getInstance().register(factory);
        } catch (Throwable t) {
            LOGGER.warn("Curios is loaded but the shell state integration failed to initialize", t);
        }
    }
}

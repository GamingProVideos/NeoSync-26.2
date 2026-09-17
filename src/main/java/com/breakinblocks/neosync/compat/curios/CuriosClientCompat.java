package com.breakinblocks.neosync.compat.curios;

import com.breakinblocks.neosync.api.shell.ShellState;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class CuriosClientCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MethodHandle DRESS_SHELL = CuriosCompat.isLoaded() ? findDressShell() : null;

    private static boolean warned;

    private CuriosClientCompat() {}

    /**
     * Puts the curios a stored shell was wearing onto its client-side stand-in, so Curios renders
     * them the same way it renders them on a player.
     */
    public static void dressShell(LivingEntity shellEntity, ShellState state) {
        if (DRESS_SHELL == null) {
            return;
        }

        try {
            DRESS_SHELL.invokeExact(shellEntity, state);
        } catch (Throwable t) {
            if (!warned) {
                warned = true;
                LOGGER.warn("Curios could not be applied to a stored shell; stored shells will render without them", t);
            }
        }
    }

    private static MethodHandle findDressShell() {
        try {
            Class<?> cls = Class.forName("com.breakinblocks.neosync.compat.curios.client.ShellCurios");
            return MethodHandles.lookup().findStatic(cls, "dress",
                    MethodType.methodType(void.class, LivingEntity.class, ShellState.class));
        } catch (Throwable t) {
            LOGGER.warn("Curios is loaded but stored shells will render without their curios", t);
            return null;
        }
    }
}

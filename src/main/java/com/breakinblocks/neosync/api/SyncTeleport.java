package com.breakinblocks.neosync.api;

public final class SyncTeleport {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private SyncTeleport() {
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    public static void begin() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void end() {
        DEPTH.set(Math.max(0, DEPTH.get() - 1));
    }
}

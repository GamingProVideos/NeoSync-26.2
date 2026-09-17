package com.breakinblocks.neosync.common.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.stream.StreamSupport;

public final class WorldUtil {
    public static Identifier getId(Level world) {
        return world.dimension().identifier();
    }

    public static boolean isOf(Identifier id, Level world) {
        return world.dimension().identifier().equals(id);
    }

    public static <T extends Level> Optional<T> findWorld(Iterable<T> worlds, Identifier id) {
        return StreamSupport.stream(worlds.spliterator(), false)
                .filter(world -> isOf(id, world))
                .findAny();
    }
}
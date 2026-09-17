package com.breakinblocks.neosync.common.utils.nbt;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;

public class NbtSerializerFactory<T> {
    private final Iterable<BiConsumer<T, CompoundTag>> readers;
    private final Iterable<BiConsumer<T, CompoundTag>> writers;

    public NbtSerializerFactory(Iterable<BiConsumer<T, CompoundTag>> readers, Iterable<BiConsumer<T, CompoundTag>> writers) {
        this.readers = ImmutableList.copyOf(readers);
        this.writers = ImmutableList.copyOf(writers);
    }

    public NbtSerializer<T> create(T target) {
        return new NbtSerializer<>(target, this.readers, this.writers);
    }
}
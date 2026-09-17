package com.breakinblocks.neosync.common.utils.nbt;

import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;

public class NbtSerializer<T> {
    private final T target;
    private final Iterable<BiConsumer<T, CompoundTag>> readers;
    private final Iterable<BiConsumer<T, CompoundTag>> writers;

    public NbtSerializer(T target, Iterable<BiConsumer<T, CompoundTag>> readers, Iterable<BiConsumer<T, CompoundTag>> writers) {
        this.target = target;
        this.readers = readers;
        this.writers = writers;
    }

    public void readNbt(CompoundTag nbt) {
        for (BiConsumer<T, CompoundTag> x : readers) {
            x.accept(this.target, nbt);
        }
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        for (BiConsumer<T, CompoundTag> x : writers) {
            x.accept(this.target, nbt);
        }
        return nbt;
    }
}
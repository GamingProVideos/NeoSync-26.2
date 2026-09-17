package com.breakinblocks.neosync.common.utils.nbt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NbtSerializerFactoryBuilder<TTarget> {
    private static final Map<Class<?>, BiFunction<CompoundTag, String, ?>> NBT_GETTERS;
    private static final Map<Class<?>, TriConsumer<CompoundTag, String, ?>> NBT_SETTERS;

    private final Collection<BiConsumer<TTarget, CompoundTag>> readers;
    private final Collection<BiConsumer<TTarget, CompoundTag>> writers;

    public NbtSerializerFactoryBuilder() {
        this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public <TProperty> NbtSerializerFactoryBuilder<TTarget> add(Class<TProperty> type, String key, Function<TTarget, TProperty> getter, BiConsumer<TTarget, TProperty> setter) {
        if (getter != null) {
            TriConsumer<CompoundTag, String, TProperty> nbtSetter = (TriConsumer<CompoundTag, String, TProperty>)NBT_SETTERS.get(type);
            if (nbtSetter == null) {
                throw new UnsupportedOperationException();
            }
            this.writers.add((i, x) -> nbtSetter.accept(x, key, getter.apply(i)));
        }

        if (setter != null) {
            BiFunction<CompoundTag, String, TProperty> nbtGetter = (BiFunction<CompoundTag, String, TProperty>)NBT_GETTERS.get(type);
            if (nbtGetter == null) {
                throw new UnsupportedOperationException();
            }
            this.readers.add((i, x) -> setter.accept(i, nbtGetter.apply(x, key)));
        }

        return this;
    }

    public NbtSerializerFactory<TTarget> build() {
        return new NbtSerializerFactory<>(this.readers, this.writers);
    }

    private static TriConsumer<CompoundTag, String, ?> setIfNotNull(TriConsumer<CompoundTag, String, Object> f) {
        return (nbt, key, x) -> {
            if (x != null) {
                f.accept(nbt, key, x);
            }
        };
    }

    static {
        NBT_GETTERS = new HashMap<>();
        NBT_GETTERS.put(Boolean.class, (nbt, key) -> nbt.getBoolean(key).orElse(false));
        NBT_GETTERS.put(Byte.class, (nbt, key) -> nbt.getByte(key).orElse((byte)0));
        NBT_GETTERS.put(Double.class, (nbt, key) -> nbt.getDouble(key).orElse(0D));
        NBT_GETTERS.put(Float.class, (nbt, key) -> nbt.getFloat(key).orElse(0F));
        NBT_GETTERS.put(Integer.class, (nbt, key) -> nbt.getInt(key).orElse(0));
        NBT_GETTERS.put(Long.class, (nbt, key) -> nbt.getLong(key).orElse(0L));
        NBT_GETTERS.put(Short.class, (nbt, key) -> nbt.getShort(key).orElse((short)0));
        NBT_GETTERS.put(String.class, (nbt, key) -> nbt.getString(key).orElse(null));
        NBT_GETTERS.put(Identifier.class, (nbt, key) -> nbt.getString(key).map(Identifier::parse).orElse(null));
        NBT_GETTERS.put(UUID.class, (nbt, key) -> nbt.read(key, UUIDUtil.CODEC).orElse(null));
        NBT_GETTERS.put(CompoundTag.class, (nbt, key) -> nbt.getCompound(key).orElse(null));
        NBT_GETTERS.put(ListTag.class, (nbt, key) -> {
            if (!nbt.contains(key)) return null;
            return nbt.get(key) instanceof ListTag list ? list : null;
        });
        NBT_GETTERS.put(BlockPos.class, (nbt, key) -> nbt.getCompound(key).map(compound -> new BlockPos(
                compound.getIntOr("x", 0),
                compound.getIntOr("y", 0),
                compound.getIntOr("z", 0)
        )).orElse(null));

        NBT_SETTERS = new HashMap<>();
        NBT_SETTERS.put(Boolean.class, setIfNotNull((nbt, key, x) -> nbt.putBoolean(key, (boolean)x)));
        NBT_SETTERS.put(Byte.class, setIfNotNull((nbt, key, x) -> nbt.putByte(key, (byte)x)));
        NBT_SETTERS.put(Double.class, setIfNotNull((nbt, key, x) -> nbt.putDouble(key, (double)x)));
        NBT_SETTERS.put(Float.class, setIfNotNull((nbt, key, x) -> nbt.putFloat(key, (float)x)));
        NBT_SETTERS.put(Integer.class, setIfNotNull((nbt, key, x) -> nbt.putInt(key, (int)x)));
        NBT_SETTERS.put(Long.class, setIfNotNull((nbt, key, x) -> nbt.putLong(key, (long)x)));
        NBT_SETTERS.put(Short.class, setIfNotNull((nbt, key, x) -> nbt.putShort(key, (short)x)));
        NBT_SETTERS.put(String.class, setIfNotNull((nbt, key, x) -> nbt.putString(key, (String)x)));
        NBT_SETTERS.put(Identifier.class, setIfNotNull((nbt, key, x) -> nbt.putString(key, x.toString())));
        NBT_SETTERS.put(UUID.class, setIfNotNull((nbt, key, x) -> nbt.store(key, UUIDUtil.CODEC, (UUID) x)));
        NBT_SETTERS.put(CompoundTag.class, setIfNotNull((nbt, key, x) -> nbt.put(key, (CompoundTag)x)));
        NBT_SETTERS.put(ListTag.class, setIfNotNull((nbt, key, x) -> nbt.put(key, (ListTag)x)));
        NBT_SETTERS.put(BlockPos.class, setIfNotNull((nbt, key, x) -> {
            BlockPos pos = (BlockPos)x;
            CompoundTag compound = new CompoundTag();
            compound.putInt("x", pos.getX());
            compound.putInt("y", pos.getY());
            compound.putInt("z", pos.getZ());
            nbt.put(key, compound);
        }));
    }

    @FunctionalInterface
    private interface TriConsumer<T, K, V> {
        void accept(T arg1, K arg2, V arg3);
    }
}

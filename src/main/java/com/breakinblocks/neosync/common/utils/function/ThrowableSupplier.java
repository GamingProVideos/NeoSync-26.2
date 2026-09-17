package com.breakinblocks.neosync.common.utils.function;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    T get() throws Throwable;
}
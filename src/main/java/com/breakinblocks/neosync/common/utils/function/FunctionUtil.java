package com.breakinblocks.neosync.common.utils.function;

import java.util.Optional;

/**
 * Utility for handling checked exceptions in functional programming contexts.
 *
 * Java's checked exceptions can be problematic when working with lambdas and method references,
 * as they force try-catch blocks even when exceptions are unlikely or impossible.
 */
public final class FunctionUtil {
    public static <T> Optional<T> tryInvoke(ThrowableSupplier<T> func) {
        try {
            return Optional.ofNullable(func.get());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Invokes a function that may throw checked exceptions, wrapping any
     * checked exceptions in RuntimeException for cleaner functional code.
     */
    public static <T> T invoke(ThrowableSupplier<T> func) {
        try {
            return func.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private FunctionUtil() {
    }
}
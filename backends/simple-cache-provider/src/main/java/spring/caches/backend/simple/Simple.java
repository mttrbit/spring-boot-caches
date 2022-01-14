package spring.caches.backend.simple;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.FormatMethod;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.cache.support.NullValue;
import spring.caches.backend.simple.stats.CacheStats;
import spring.caches.backend.simple.stats.ConcurrentStatsCounter;
import spring.caches.backend.simple.stats.StatsCounter;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Simple builder for creating instances of {@link UnboundedSimpleCache}.
 */
final class Simple {

    public static final int UNSET_INT = -1;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final Supplier<StatsCounter> ENABLED_STATS_COUNTER_SUPPLIER = ConcurrentStatsCounter::new;
    private int initialCapacity = UNSET_INT;

    private boolean allowNullValues = true;
    private @Nullable Supplier<StatsCounter> statsCounterSupplier;

    @FormatMethod
    static void requireArgument(boolean expression, String template, @Nullable Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(template, args));
        }
    }

    /**
     * Ensures that the argument expression is true.
     */
    static void requireArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static Simple newBuilder() {
        return new Simple();
    }

    /**
     * Constructs a new {@code Simple} instance with the settings specified in {@code spec}.
     *
     * @param spec a String in the format specified by {@link SimpleSpec}
     * @return a new instance with the specification's settings
     */
    @CheckReturnValue
    public static Simple from(String spec) {
        return from(SimpleSpec.parse(spec));
    }

    @CheckReturnValue
    public static Simple from(SimpleSpec spec) {
        return spec.toBuilder();
    }

    /**
     * Sets the minimum total size for the internal data structures. Providing a large enough estimate
     * at construction time avoids the need for expensive resizing operations later, but setting this
     * value unnecessarily high wastes memory.
     *
     * @param initialCapacity minimum total size for the internal data structures
     * @return this {@code Simple} instance (for chaining)
     * @throws IllegalArgumentException if {@code initialCapacity} is negative
     */
    public Simple initialCapacity(@NonNegative int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }

    /**
     * Specify whether to accept and convert null values for all caches in this cache manager.
     * Default is "true". An internal holder object will be used to store user-level nulls.
     * See {@link NullValue}
     *
     * @return this {@code Simple} instance (for chaining)
     */
    public Simple allowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
        return this;
    }

    boolean allowsNullValues() {
        return allowNullValues;
    }

    boolean hasInitialCapacity() {
        return (initialCapacity != UNSET_INT);
    }

    int getInitialCapacity() {
        return hasInitialCapacity() ? initialCapacity : DEFAULT_INITIAL_CAPACITY;
    }

    public Cache build() {
        return new UnboundedSimpleCache(this);
    }

    /**
     * Enables the accumulation of {@link CacheStats} during the operation of the cache. Without this
     * {@link Cache#stats} will return zero for all statistics. Note that recording statistics
     * requires bookkeeping to be performed with each operation, and thus imposes a performance
     * penalty on cache operation.
     *
     * @return this {@code Simple} instance (for chaining)
     */
    public Simple recordStats() {
        statsCounterSupplier = ENABLED_STATS_COUNTER_SUPPLIER;
        return this;
    }

    /**
     * Enables the accumulation of {@link CacheStats} during the operation of the cache. Without this
     * {@link Cache#stats} will return zero for all statistics. Note that recording statistics
     * requires bookkeeping to be performed with each operation, and thus imposes a performance
     * penalty on cache operation. Any exception thrown by the supplied {@link StatsCounter} will be
     * suppressed and logged.
     *
     * @param statsCounterSupplier a supplier instance that returns a new {@link StatsCounter}
     * @return this {@code Simple} instance (for chaining)
     */
    public Simple recordStats(Supplier<? extends StatsCounter> statsCounterSupplier) {
        requireNonNull(statsCounterSupplier);
        this.statsCounterSupplier = () -> StatsCounter.guardedStatsCounter(statsCounterSupplier.get());
        return this;
    }

    boolean isRecordingStats() {
        return (statsCounterSupplier != null);
    }

    Supplier<StatsCounter> getStatsCounterSupplier() {
        return (statsCounterSupplier == null)
                ? StatsCounter::disabledStatsCounter
                : statsCounterSupplier;
    }
}

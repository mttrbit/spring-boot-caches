package spring.caches.backend.elasticache;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.FormatMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import spring.caches.backend.elasticache.engines.memcached.stats.CacheStats;
import spring.caches.backend.elasticache.engines.memcached.stats.ConcurrentStatsCounter;
import spring.caches.backend.elasticache.engines.memcached.stats.StatsCounter;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * tbd.
 */
public final class ElastiCache {

    public static final int UNSET_INT = -1;
    private static final Supplier<StatsCounter> ENABLED_STATS_COUNTER_SUPPLIER = ConcurrentStatsCounter::new;
    private static final int DEFAULT_EXPIRATION = 60;
    private @Nullable Supplier<StatsCounter> statsCounterSupplier;
    private int expiration = UNSET_INT;

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

    public static ElastiCache newBuilder() {
        return new ElastiCache();
    }

    /**
     * Constructs a new {@code Simple} instance with the settings specified in {@code spec}.
     *
     * @param spec a String in the format specified by {@link ElastiCacheSpec}
     * @return a new instance with the specification's settings
     */
    @CheckReturnValue
    public static ElastiCache from(String spec) {
        return from(ElastiCacheSpec.parse(spec));
    }

    @CheckReturnValue
    public static ElastiCache from(ElastiCacheSpec spec) {
        return spec.toBuilder();
    }

    boolean hasExpiration() {
        return (expiration != UNSET_INT);
    }

    public int expiration() {
        return hasExpiration() ? expiration : DEFAULT_EXPIRATION;
    }

    public ElastiCache expiration(int expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Enables the accumulation of {@link CacheStats} during the operation of the cache. Without this
     * {@link spring.caches.backend.elasticache.engines.memcached.MemcachedCache#stats} will return zero for all
     * statistics. Note that recording statistics requires bookkeeping to be performed with each operation, and thus
     * imposes a performance penalty on cache operation.
     *
     * @return this {@code Simple} instance (for chaining)
     */
    public ElastiCache recordStats() {
        statsCounterSupplier = ENABLED_STATS_COUNTER_SUPPLIER;
        return this;
    }

    /**
     * Enables the accumulation of {@link CacheStats} during the operation of the cache. Without this
     * {@link spring.caches.backend.elasticache.engines.memcached.MemcachedCache#stats} will return zero for all
     * statistics. Note that recording statistics requires bookkeeping to be performed with each operation, and thus
     * imposes a performance penalty on cache operation. Any exception thrown by the supplied {@link StatsCounter} will
     * be suppressed and logged.
     *
     * @param statsCounterSupplier a supplier instance that returns a new {@link StatsCounter}
     * @return this {@code Simple} instance (for chaining)
     */
    public ElastiCache recordStats(Supplier<? extends StatsCounter> statsCounterSupplier) {
        requireNonNull(statsCounterSupplier);
        this.statsCounterSupplier = () -> StatsCounter.guardedStatsCounter(statsCounterSupplier.get());
        return this;
    }

    public boolean isRecordingStats() {
        return (statsCounterSupplier != null);
    }

    public StatsCounter statsCounter() {
        return getStatsCounterSupplier().get();
    }

    Supplier<StatsCounter> getStatsCounterSupplier() {
        return (statsCounterSupplier == null)
                ? StatsCounter::disabledStatsCounter
                : statsCounterSupplier;
    }
}

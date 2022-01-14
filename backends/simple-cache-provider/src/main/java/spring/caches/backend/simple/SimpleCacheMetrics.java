package spring.caches.backend.simple;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import spring.caches.backend.simple.stats.CacheStats;

/**
 * Explain.
 */
@NonNullApi
@NonNullFields
class SimpleCacheMetrics extends CacheMeterBinder {
    private final Cache cache;

    /**
     * Creates a new {@link SimpleCacheMetrics} instance.
     *
     * @param cache     The cache to be instrumented. You must call {@link Simple#recordStats()} prior to building
     *                  the cache for metrics to be recorded.
     * @param cacheName Will be used to tag metrics with "cache".
     * @param tags      tags to apply to all recorded metrics.
     */
    SimpleCacheMetrics(Cache cache, String cacheName, Iterable<Tag> tags) {
        super(cache, cacheName, tags);
        this.cache = cache;
    }

    /**
     * Record metrics on a Caffeine cache. You must call {@link Simple#recordStats()} prior to building the cache
     * for metrics to be recorded.
     *
     * @param registry  The registry to bind metrics to.
     * @param cache     The cache to instrument.
     * @param cacheName Will be used to tag metrics with "cache".
     * @param tags      Tags to apply to all recorded metrics. Must be an even number of arguments representing
     *                  key/value pairs of tags.
     * @param <C>       The cache type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in any way.
     */
    public static <C extends Cache> C monitor(MeterRegistry registry, C cache, String cacheName, String... tags) {
        return monitor(registry, cache, cacheName, Tags.of(tags));
    }

    /**
     * Record metrics on a Caffeine cache. You must call {@link Simple#recordStats()} prior to building the cache
     * for metrics to be recorded.
     *
     * @param registry  The registry to bind metrics to.
     * @param cache     The cache to instrument.
     * @param cacheName Will be used to tag metrics with "cache".
     * @param tags      Tags to apply to all recorded metrics.
     * @param <C>       The cache type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in any way.
     * @see CacheStats
     */
    public static <C extends Cache> C monitor(
            MeterRegistry registry,
            C cache,
            String cacheName,
            Iterable<Tag> tags
    ) {
        new SimpleCacheMetrics(cache, cacheName, tags).bindTo(registry);
        return cache;
    }

    @Override
    protected Long size() {
        return cache.estimatedSize();
    }

    @Override
    protected long hitCount() {
        return cache.stats().hitCount();
    }

    @Override
    protected Long missCount() {
        return cache.stats().missCount();
    }

    @Override
    protected Long evictionCount() {
        return cache.stats().evictionCount();
    }

    @Override
    protected long putCount() {
        return cache.stats().loadCount();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        // Intentionally left empty.
    }
}

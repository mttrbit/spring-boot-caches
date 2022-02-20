package spring.caches.backend.elasticache.engines.memcached;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import net.spy.memcached.MemcachedClientIF;

/**
 * Collect metrics on Memcached caches.
 */
public class MemcachedCacheMetrics extends CacheMeterBinder {
    private final MemcachedCache cache;

    /**
     * Creates a new {@link CaffeineCacheMetrics} instance.
     *
     * @param cache The memcached cache to be instrumented.
     * @param tags  tags to apply to all recorded metrics.
     */
    public MemcachedCacheMetrics(MemcachedCache cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
        this.cache = cache;
    }

    @Override
    protected Long size() {
        // MemcachedCache doesn't support size
        return null;
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
        if (cache.getNativeCache() instanceof MemcachedClientIF) {
            final MemcachedClientIF memcachedClient = (MemcachedClientIF) cache.getNativeCache();

            registry.gauge("available_servers_count", memcachedClient.getAvailableServers().size());
        }
    }
}

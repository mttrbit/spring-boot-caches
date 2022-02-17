package spring.caches.backend.elasticache.engines.memcached;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;

/**
 * Memcached {@link CacheMeterBinderProvider}.
 */
public class MemcachedCacheMeterBinderProvider implements CacheMeterBinderProvider<MemcachedCache> {

    @Override
    public CacheMeterBinder getMeterBinder(MemcachedCache memcachedCache, Iterable<Tag> tags) {
        return new MemcachedCacheMetrics(memcachedCache, tags);
    }
}
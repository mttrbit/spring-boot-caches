package spring.caches.backend.simple;

import spring.caches.backend.simple.stats.CacheStats;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * Why is this abstraction needed?
 */
public interface Cache {

    Object get(Object key);

    Object get(Object key, Callable<Object> valueLoader);

    Object put(Object key, Object value);

    Object putIfAbsent(Object key, Object value);

    CacheStats stats();

    long estimatedSize();

    ConcurrentMap<Object, Object> asMap();

    void invalidate(Object key);

    /**
     * Discards all entries in the cache.
     */
    void invalidateAll();

    /**
     * Discards all entries in the cache.
     */
    void invalidateAll(Iterable<?> keys);
}

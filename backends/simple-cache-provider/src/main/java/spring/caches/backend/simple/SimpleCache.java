package spring.caches.backend.simple;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

class SimpleCache extends AbstractValueAdaptingCache {
    private final String name;

    private final Cache cache;

    /**
     * Create a {@link SimpleCache} instance with the specified name and the
     * given internal {@link Cache} to use.
     *
     * @param name  the name of the cache
     * @param cache the backing Caffeine Cache instance
     */
    SimpleCache(String name, Cache cache) {
        this(name, cache, true);
    }

    /**
     * Create a {@link SimpleCache} instance with the specified name and the
     * given internal {@link Cache} to use.
     *
     * @param name            the name of the cache
     * @param cache           the backing Caffeine Cache instance
     * @param allowNullValues whether to accept and convert {@code null}
     *                        values for this cache
     */
    SimpleCache(
            String name,
            Cache cache,
            boolean allowNullValues
    ) {
        super(allowNullValues);
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(cache, "Cache must not be null");
        this.name = name;
        this.cache = cache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) cache.get(key, (Callable<Object>) valueLoader);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return toValueWrapper(cache.putIfAbsent(key, value));
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Cache getNativeCache() {
        return this.cache;
    }

    @Override
    @Nullable
    protected Object lookup(Object key) {
        return this.cache.get(key);
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.cache.put(key, toStoreValue(value));
    }

    @Override
    public void evict(Object key) {
        this.cache.invalidate(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return (this.cache.asMap().remove(key) != null);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    @Override
    public boolean invalidate() {
        boolean notEmpty = !this.cache.asMap().isEmpty();
        this.cache.invalidateAll();
        return notEmpty;
    }
}

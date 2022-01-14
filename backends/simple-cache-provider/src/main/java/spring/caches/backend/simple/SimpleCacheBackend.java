package spring.caches.backend.simple;

import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.cache.CacheManager;
import spring.caches.backend.CacheBackend;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A caching backend that uses {@link Simple}.
 */
final class SimpleCacheBackend extends CacheBackend {
    private final SimpleCacheManager cacheManager;

    private SimpleCacheBackend(SimpleCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public static SimpleCacheBackend of(Map<String, Simple> settings) {
        return new SimpleCacheBackend(new SimpleCacheManager(settings));
    }

    @Override
    public String getBackendName() {
        return "simple";
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void injectCacheMeterBinderProvider(BiConsumer<String, Object> consumer) {
        consumer.accept(
                getBackendName(),
                (CacheMeterBinderProvider<SimpleCache>) (cache, tags) ->
                        new SimpleCacheMetrics(cache.getNativeCache(), cache.getName(), tags));
    }
}

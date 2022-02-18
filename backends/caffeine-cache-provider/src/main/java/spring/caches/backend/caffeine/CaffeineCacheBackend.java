package spring.caches.backend.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.cache.CacheManager;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.system.CacheBackendInstantiationException;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A caching backend that uses {@code Caffeine}.
 */
final class CaffeineCacheBackend extends CacheBackend {

    private final CaffeineCacheManager cacheManager;

    private CaffeineCacheBackend(CaffeineCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Creates a new instance of a {@code CacheBackend} using the provided configuration data.
     * Throws a {@code CacheBackendInstantiationException} when the configuration data is invalid.
     */
    public static CaffeineCacheBackend of(Map<String, Caffeine<Object, Object>> settings) {
        if (settings.isEmpty()) {
            throw new CacheBackendInstantiationException("Invalid cache backend configuration!");
        }
        return new CaffeineCacheBackend(new CaffeineCacheManager(settings));
    }

    @Override
    public String getBackendName() {
        return "caffeine";
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void injectCacheMeterBinderProvider(BiConsumer<String, Object> consumer) {
        consumer.accept(
                getBackendName(),
                (CacheMeterBinderProvider<CaffeineCache>) (cache, tags) ->
                        new CaffeineCacheMetrics(cache.getNativeCache(), cache.getName(), tags));
    }
}

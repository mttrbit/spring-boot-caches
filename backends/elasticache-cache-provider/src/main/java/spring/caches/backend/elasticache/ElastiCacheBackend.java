package spring.caches.backend.elasticache;

import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.elasticache.engines.memcached.MemcachedCache;
import spring.caches.backend.elasticache.engines.memcached.MemcachedCacheMetrics;
import spring.caches.backend.elasticache.engines.redis.RedisCacheMetrics;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A caching backend that uses {@code ElastiCache}.
 */
final class ElastiCacheBackend extends CacheBackend {

    private final CacheManager cacheManager;

    private ElastiCacheBackend(ElastiCacheCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public static ElastiCacheBackend of(List<Cache> caches) {
        ElastiCacheCacheManager cacheManager = new ElastiCacheCacheManager();
        cacheManager.setCaches(caches);
        cacheManager.afterPropertiesSet();
        return new ElastiCacheBackend(cacheManager);
    }

    @Override
    public String getBackendName() {
        return "elasticache";
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void injectCacheMeterBinderProvider(BiConsumer<String, Object> consumer) {
        consumer.accept(
                getBackendName() + "redis",
                (CacheMeterBinderProvider<RedisCache>) RedisCacheMetrics::new
        );
        consumer.accept(
                getBackendName() + "memcached",
                (CacheMeterBinderProvider<MemcachedCache>) MemcachedCacheMetrics::new
        );
    }
}

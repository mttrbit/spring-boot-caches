package spring.caches.backend.elasticache.engines;

import org.springframework.cache.Cache;

/**
 * tbd.
 */
public interface CacheFactory {

    boolean isSupportingCacheArchitecture(String architecture);

    Cache createCache(String cacheName, String host, int port) throws Exception;
}

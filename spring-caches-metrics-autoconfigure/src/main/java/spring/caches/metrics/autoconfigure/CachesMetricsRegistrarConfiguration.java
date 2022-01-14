package spring.caches.metrics.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Configure a {@link CacheMetricsRegistrar} and register all available {@link Cache
 * caches}.
 * <p>
 * Partially duplicates functionality from Spring Boot's package-private class
 * {@code org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsRegistrarConfiguration}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MeterRegistry.class)
class CachesMetricsRegistrarConfiguration {
    private static final String CACHE_MANAGER = "cacheManager";
    private static final String CACHE_MANAGER_SUFFIX = CACHE_MANAGER;
    private static final String TAG_NAME = CACHE_MANAGER;

    private final CacheMetricsRegistrar cacheMetricsRegistrar;

    private final Map<String, CacheManager> cacheManagers;

    CachesMetricsRegistrarConfiguration(
            MeterRegistry registry,
            Collection<CacheMeterBinderProvider<?>> binderProviders,
            Map<String, CacheManager> cacheManagers
    ) {
        this.cacheManagers = cacheManagers;
        this.cacheMetricsRegistrar = new CacheMetricsRegistrar(registry, binderProviders);
        bindCachesToRegistry();
    }

    @Bean
    public CacheMetricsRegistrar cacheMetricsRegistrar() {
        return this.cacheMetricsRegistrar;
    }

    private void bindCachesToRegistry() {
        this.cacheManagers.forEach(this::bindCacheManagerToRegistry);
    }

    private void bindCacheManagerToRegistry(String beanName, CacheManager cacheManager) {
        for (String cacheName : cacheManager.getCacheNames()) {
            bindCacheToRegistry(beanName, cacheManager.getCache(cacheName));
        }
    }

    private void bindCacheToRegistry(String beanName, Cache cache) {
        this.cacheMetricsRegistrar.bindCacheToRegistry(cache, Tag.of(TAG_NAME, getCacheManagerName(beanName)));
    }

    /**
     * Get the name of a {@link CacheManager} based on its {@code beanName}.
     *
     * @param beanName the name of the {@link CacheManager} bean
     * @return a name for the given cache manager
     */
    private String getCacheManagerName(String beanName) {
        if (beanName.length() > CACHE_MANAGER_SUFFIX.length()
                && StringUtils.endsWithIgnoreCase(beanName, CACHE_MANAGER_SUFFIX)) {
            return beanName.substring(0, beanName.length() - CACHE_MANAGER_SUFFIX.length());
        }
        return beanName;
    }
}

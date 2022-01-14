package spring.caches.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EnableCaching
@Configuration(proxyBeanMethods = false)
class CacheResolverConfiguration extends CachingConfigurerSupport {

    private final Map<String, CacheManager> cacheManagers;

    CacheResolverConfiguration(Map<String, CacheManager> cacheManagers) {
        this.cacheManagers = cacheManagers;
    }

    /**
     * Sets the default {@link CacheResolver} used when annotating classes or methods with
     * {@link org.springframework.cache.annotation.Cacheable}.
     */
    @Bean("springCachesResolver")
    @ConditionalOnMissingBean(name = "springCachesResolver")
    @Override
    public CacheResolver cacheResolver() {
        final Map<String, Cache> caches = new ConcurrentHashMap<>(16);

        for (CacheManager cacheManager : cacheManagers.values()) {
            for (String cacheName : cacheManager.getCacheNames()) {
                caches.computeIfAbsent(cacheName, cacheManager::getCache);
            }
        }

        return new DefaultCachesResolver(caches);
    }

    private static class DefaultCachesResolver implements CacheResolver {

        private final Map<String, Cache> caches;

        DefaultCachesResolver(Map<String, Cache> caches) {
            this.caches = caches;
        }

        @Override
        @NonNull
        public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
            final Set<String> cacheNames = getCacheNames(context);
            if (cacheNames.isEmpty()) {
                return Collections.emptyList();
            }

            Collection<Cache> resolved = new ArrayList<>(cacheNames.size());
            for (String name : cacheNames) {
                final Cache cache = caches.get(name);
                if (cache == null) {
                    throw new IllegalArgumentException(
                            String.format("Cannot find cache named '%s' for %s", name, context.getOperation())
                    );
                }
                resolved.add(cache);
            }
            return resolved;
        }

        protected Set<String> getCacheNames(CacheOperationInvocationContext<?> context) {
            return context.getOperation().getCacheNames();
        }
    }
}

package spring.caches.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.Platform;
import spring.caches.backend.properties.tree.MultiCacheProperties;
import spring.caches.backend.system.BackendFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;

@Configuration(proxyBeanMethods = false)
class CachesConfiguration {
    private static final Log LOG = LogFactory.getLog(CachesConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    static class PlatformConfiguration {

        private static final String PREFIX_KEY = "spring.caches.";
        private static final int OFFSET = "spring.".length();
        private static final String[] SOURCES = {
                "test", // used by ApplicationContextRunner#withPropertyValues
                "applicationConfig: ",
                "Inlined Test Properties"
        };
        private final Map<String, CacheManager> cacheManagerMap;
        private final ConfigurableApplicationContext configurableApplicationContext;
        private final Map<String, Object> cacheMeterBinderProviderMap;

        PlatformConfiguration(Environment environment, ApplicationContext applicationContext) {
            MultiCacheProperties properties = multiCacheProperties(environment);
            List<CacheBackend> cacheBackends = loadCacheBackends(properties, applicationContext);
            cacheMeterBinderProviderMap = loadBinderProviderMap(cacheBackends);
            configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            cacheManagerMap = loadCacheManagerMap(cacheBackends);

            registerCacheMeterBinderProviders();
            registerCacheManagers();
        }

        @Bean
        @ConditionalOnMissingBean(name = "cacheManagers")
        Map<String, CacheManager> cacheManagers() {
            return cacheManagerMap;
        }

        final List<CacheBackend> loadCacheBackends(
                MultiCacheProperties properties,
                ApplicationContext applicationContext
        ) {
            List<CacheBackend> cacheBackends = new LinkedList<>();
            Platform.getBackendFactoryNames().forEach(f -> {
                MultiCacheProperties filtered = properties.filterByFactoryName(f);
                if (filtered.isEmpty()) {
                    LOG.warn("cache_backend=" + f + " not loaded -> configuration is missing.");
                } else {
                    BackendFactory backendFactory = Platform.getBackend(f);

                    if (backendFactory instanceof ApplicationContextAware) {
                        ((ApplicationContextAware) backendFactory).setApplicationContext(applicationContext);
                    }

                    cacheBackends.add(backendFactory.create(filtered));
                }
            });

            return cacheBackends;
        }

        final Map<String, Object> loadBinderProviderMap(List<CacheBackend> cacheBackends) {
            Map<String, Object> binderProviderMap = new LinkedHashMap<>();
            cacheBackends.forEach(backend -> backend.injectCacheMeterBinderProvider(binderProviderMap::put));
            return binderProviderMap;
        }

        final Map<String, CacheManager> loadCacheManagerMap(List<CacheBackend> cacheBackends) {
            Map<String, CacheManager> cacheManagerMap = new LinkedHashMap<>(16);
            cacheBackends.forEach(backend -> backend.injectCacheManager(cacheManagerMap::put));
            return cacheManagerMap;
        }

        final MultiCacheProperties multiCacheProperties(Environment environment) {
            return MultiCacheProperties.load(
                    resolveProperties(ConfigurationPropertySources.get(environment)),
                    e -> e.getKey().startsWith(PREFIX_KEY),
                    OFFSET
            );
        }

        final void registerCacheMeterBinderProviders() {
            cacheMeterBinderProviderMap.forEach(this::registerSingleton);
        }

        final void registerCacheManagers() {
            cacheManagerMap.forEach((key, value) -> {
                String beanName = key.toLowerCase(Locale.ENGLISH) + "CacheManager";
                registerSingleton(beanName, value);
            });
        }

        /**
         * Register the given existing object as singleton in the bean registry,
         * under the given bean name.
         */
        final <T> void registerSingleton(String name, T object) {
            final SingletonBeanRegistry registry = configurableApplicationContext.getBeanFactory();
            if (!registry.containsSingleton(name)) {
                registry.registerSingleton(name, object);
            }
        }

        private Map<String, Object> resolveProperties(Iterable<ConfigurationPropertySource> sources) {
            return StreamSupport.stream(sources.spliterator(), false)
                    .map(ConfigurationPropertySource::getUnderlyingSource)
                    .filter(source -> source instanceof MapPropertySource)
                    .map(source -> (MapPropertySource) source)
                    .filter(source -> filterSourcesByName(source, SOURCES))
                    .collect(HashMap::new, (m, n) -> m.putAll(n.getSource()), Map::putAll);
        }

        private boolean filterSourcesByName(MapPropertySource source, String... names) {
            for (String name : names) {
                if (source.getName().startsWith(name)) {
                    return true;
                }
            }

            return false;
        }
    }

}

package spring.caches.backend.system;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.CachesProperties;
import spring.caches.backend.properties.tree.Node;
import spring.caches.backend.properties.tree.Tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Default factory for creating cache backends.
 *
 * <p>See class documentation in {@link BackendFactory} for important implementation restrictions.
 *
 * <h2>Implementation Details</h2>
 *
 * This factory is loaded only, when no other cache backend is found on classpath. The purpose of
 * this backend is to provide a very simple backend that is sufficient for prototyping. Hence,
 * it lacks caching configurability as well as support for metrics.
 *
 * Note that the application property also differs slightly as there is no need to configure map.
 * An application developer only defines the names:
 * <em>spring.caches.names=cache1,cache2...</em>
 *
 * In case support for cache metrics is required, there is also the module
 * <em>simple-cache-provider</em> which uses a {@link ConcurrentMapCacheManager}.
 */
final class DefaultBackendFactory extends BackendFactory {
    public static final String BACKEND_NAME = "default";

    private static final BackendFactory INSTANCE = new DefaultBackendFactory();

    private DefaultBackendFactory() {
    }

    // Called during caching platform initialization;
    public static BackendFactory getInstance() {
        return INSTANCE;
    }

    private static List<String> findNames(Tree t) {
        return t.find(BACKEND_NAME + ".names")
                .map(Node::getValue)
                .map(String::valueOf)
                .map(names -> Arrays.stream(names.split(",")).map(String::strip).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public CacheBackend create(CachesProperties properties) {
        List<String> names = new LinkedList<>();
        properties.consume(t -> names.addAll(findNames(t)));
        return new CacheBackend() {
            @Override
            public String getBackendName() {
                return BACKEND_NAME.toLowerCase(Locale.ENGLISH);
            }

            @Override
            public CacheManager getCacheManager() {
                ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
                cacheManager.setCacheNames(names);
                return cacheManager;
            }

            @Override
            public void injectCacheMeterBinderProvider(BiConsumer<String, Object> consumer) {
            }
        };
    }

    @Override
    public String toString() {
        return BACKEND_NAME;
    }
}

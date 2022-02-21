package spring.caches.backend.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.auto.service.AutoService;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.CachesProperties;
import spring.caches.backend.system.BackendFactory;
import spring.caches.backend.system.DefaultPlatform;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * BackendFactory for caffeine
 *
 * <p>When using Spring Cache's {@link DefaultPlatform}, this
 * factory will automatically be used if it is included on the classpath.
 */
@AutoService(BackendFactory.class)
public class CaffeineBackendFactory extends BackendFactory {

    public static final String BACKEND_NAME = "caffeine";

    // Constructs a new caffeine cache instance. If there is no cache configuration
    // provided, the default values as defined by caffeine will be used.
    private static Caffeine<Object, Object> findSpec(CachesProperties.Data data) {
        return data
                .getValue(".config.spec", String.class)
                .map(Caffeine::from)
                .orElse(Caffeine.newBuilder());
    }


    private static List<String> findNames(CachesProperties.Data data) {
        return data
                .getValue(".names", String.class)
                .map(CaffeineBackendFactory::split)
                .orElse(Collections.emptyList());
    }

    private static List<String> split(String names) {
        return Arrays.stream(names.split(",")).map(String::strip).collect(Collectors.toList());
    }

    @Override
    public CacheBackend create(CachesProperties properties) {
        Map<String, Caffeine<Object, Object>> settings = new ConcurrentHashMap<>(16);
        properties.consume(data -> {
            Caffeine<Object, Object> builder = findSpec(data);
            for (String name : findNames(data)) {
                settings.put(name, builder);
            }
        });

        return CaffeineCacheBackend.of(settings);
    }

    @Override
    public String toString() {
        return BACKEND_NAME;
    }
}

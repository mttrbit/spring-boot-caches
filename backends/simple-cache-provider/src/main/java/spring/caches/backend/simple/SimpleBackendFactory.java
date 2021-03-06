package spring.caches.backend.simple;

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
 * BThis backend factory instantiates a very simple cache based on a {@link ConcurrentHashMap}.
 *
 * <p>When using Spring Cache's {@link DefaultPlatform}, this
 * factory will automatically be used if it is included on the classpath.
 */
@AutoService(BackendFactory.class)
public class SimpleBackendFactory extends BackendFactory {

    public static final String BACKEND_NAME = "simple";

    // Constructs a new simple cache instance. If there is no cache configuration
    // provided, the default values as defined by simple will be used.
    private static Simple findSpec(CachesProperties.Data data) {
        return data.getValue(".config.spec", String.class)
                .map(Simple::from)
                .orElse(Simple.newBuilder());
    }

    private static List<String> findNames(CachesProperties.Data data) {
        return data.getValue(".names", String.class)
                .map(SimpleBackendFactory::split)
                .orElse(Collections.emptyList());
    }

    private static List<String> split(String names) {
        return Arrays.stream(names.split(",")).map(String::strip).collect(Collectors.toList());
    }

    @Override
    public CacheBackend create(CachesProperties properties) {
        Map<String, Simple> settings = new ConcurrentHashMap<>(16);
        properties.consume(data -> {
            Simple builder = findSpec(data).recordStats();
            for (String name : findNames(data)) {
                settings.put(name, builder);
            }
        });

        return SimpleCacheBackend.of(settings);
    }

    @Override
    public String toString() {
        return BACKEND_NAME;
    }
}

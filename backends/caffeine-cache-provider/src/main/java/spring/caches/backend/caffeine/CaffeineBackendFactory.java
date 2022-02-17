package spring.caches.backend.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.auto.service.AutoService;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.MultiCacheProperties;
import spring.caches.backend.properties.tree.Node;
import spring.caches.backend.properties.tree.Tree;
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
    private static Caffeine<Object, Object> findSpec(Tree t) {
        return t.find(BACKEND_NAME + ".config.spec")
                .map(Node::getValue)
                .map(String::valueOf)
                .map(Caffeine::from)
                .orElse(Caffeine.newBuilder());
    }

    private static List<String> findNames(Tree t) {
        return t.find(BACKEND_NAME + ".names")
                .map(Node::getValue)
                .map(String::valueOf)
                .map(names -> Arrays.stream(names.split(",")).map(String::strip).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public CacheBackend create(MultiCacheProperties properties) {
        Map<String, Caffeine<Object, Object>> settings = new ConcurrentHashMap<>(16);
        properties.consume(t -> {
            Caffeine<Object, Object> builder = findSpec(t);
            for (String name : findNames(t)) {
                settings.put(name, builder);
            }
        });

        if (settings.isEmpty()) {
            throw new RuntimeException("Invalid cache backend configuration!");
        }

        return CaffeineCacheBackend.of(settings);
    }

    @Override
    public String toString() {
        return BACKEND_NAME;
    }
}

package spring.caches.backend.simple;

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
 * BackendFactory for JCache
 *
 * <p>When using Spring Cache's {@link DefaultPlatform}, this
 * factory will automatically be used if it is included on the classpath.
 */
@AutoService(BackendFactory.class)
public class SimpleBackendFactory extends BackendFactory {

    public static final String BACKEND_NAME = "simple";

    // Constructs a new simple cache instance. If there is no cache configuration
    // provided, the default values as defined by simple will be used.
    // TODO do not return Simple, return SimpleConfig
    private static Simple findSpec(Tree t) {
        return t.find(BACKEND_NAME + ".config.spec")
                .map(Node::getValue)
                .map(String::valueOf)
                .map(Simple::from)
                .orElse(Simple.newBuilder());
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
        Map<String, Simple> settings = new ConcurrentHashMap<>(16);
        properties.consume(t -> {
            Simple builder = findSpec(t).recordStats();
            for (String name : findNames(t)) {
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

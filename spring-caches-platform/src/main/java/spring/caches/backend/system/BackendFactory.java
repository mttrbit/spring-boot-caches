package spring.caches.backend.system;

import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.MultiCacheProperties;

/**
 * An API to create caching backends for a given set of properties. This is implemented as an abstract class
 * (rather than an interface) to reduce to risk of breaking existing implementations if the API
 * changes.
 *
 * <h2>This is a service type</h2>
 *
 * <p>This type is considered a <i>service type</i> and implementations may be loaded from the
 * classpath via {@link java.util.ServiceLoader} provided the proper service metadata is included in
 * the jar file containing the implementation. When creating an implementation of this class, you
 * can provide service metadata (and thereby allow users to get your implementation just by
 * including your jar file) by either manually including a {@code
 * META-INF/services/spring.caches.backend.system.BackendFactory} file containing the
 * name of your implementation class or by annotating your implementation class using <a
 * href="https://github.com/google/auto/tree/master/service">
 * {@code @AutoService(BackendFactory.class)}</a>. See the documentation of both {@link
 * java.util.ServiceLoader} and {@link DefaultPlatform} for more information.
 */
public abstract class BackendFactory {

    /**
     * Creates a cache backend using a set of properties for use by a Spring Caches. Note that the
     * returned backend must be unique; there is one {@link CacheBackend} per
     * {@link org.springframework.cache.CacheManager}.
     */
    public abstract CacheBackend create(MultiCacheProperties properties);
}

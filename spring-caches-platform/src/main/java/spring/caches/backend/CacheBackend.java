package spring.caches.backend;

import org.springframework.cache.CacheManager;
import spring.caches.backend.system.BackendFactory;

import java.util.function.BiConsumer;

/**
 * Interface for all cache backends.
 */
public abstract class CacheBackend {

    /**
     * Returns the name of the cache backend.
     */
    public abstract String getBackendName();

    /**
     * Returns a {@link CacheManager} or {@code null} if not given.
     */
    public abstract CacheManager getCacheManager();

    /**
     * Method for injecting instances of
     * {@code org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider} into a
     * {@code BiConsumer<String,Object>}.
     *
     * <h2>Essential Implementation Restrictions</h2>
     * <p>
     * Any implementation of this method <em>MUST</em> follow the rules listed below to ensure the correct
     * initialization of a cache backend:
     * </p>
     *
     * <ol>
     *     <li>Implementations <em>MUST NOT</em> use a
     *     {@code BiConsumer<String, org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider<?>}.
     *     <li>The first parameter passed to the BiConsumer <em>MUST</em> be the name of the cache provider's
     *     implementation of {@link BackendFactory}</li>
     * </ol>
     *
     * <p>Note that it is an intentional design decision to use a {@code BiConsumer<String,Object>} and to shift
     * the responsibility of providing a type correct implementation to the caching provider. This way
     * we avoid creating a direct dependency to Spring Boot Actuator in the backend. This is needed as
     * users of Spring Caches may choose to not use Spring Boot Actuator in conjunction with Spring Caches.</p>
     *
     * <p>The implementor of a {@code CacheBackend} is expected to override this method
     * in order to support Spring Boot Actuator Caching Metrics.</p>
     */
    public abstract void injectCacheMeterBinderProvider(BiConsumer<String, Object> consumer);

    public void injectCacheManager(BiConsumer<String, CacheManager> consumer) {
        consumer.accept(getBackendName(), getCacheManager());
    }
}

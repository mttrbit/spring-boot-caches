package spring.caches.backend.system;

/**
 * Thrown when the {@link spring.caches.backend.Platform} attempts to load a {@link spring.caches.backend.CacheBackend}
 * and it is determined that a valid instance of a cache backend cannot be created.
 */
public class CacheBackendInstantiationException extends RuntimeException {

    public CacheBackendInstantiationException(String msg) {
        super(msg);
    }

    public CacheBackendInstantiationException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}

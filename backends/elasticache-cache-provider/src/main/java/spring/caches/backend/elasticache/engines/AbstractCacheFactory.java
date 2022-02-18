package spring.caches.backend.elasticache.engines;

import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <T> connection client type
 */
public abstract class AbstractCacheFactory<T> implements CacheFactory {

    private final Map<String, T> nativeConnectionClients = new HashMap<>();

    private final Map<String, Integer> expiryTimePerCache = new HashMap<>();

    private int expiryTime;

    protected AbstractCacheFactory() {
    }

    protected AbstractCacheFactory(Map<String, Integer> expiryTimePerCache, int expiryTime) {
        this.setExpiryTimePerCache(expiryTimePerCache);
        this.setExpiryTime(expiryTime);
    }

    protected abstract void destroyConnectionClient(T connectionClient) throws Exception;

    protected final T getConnectionFactory(String hostName, int port) throws Exception {
        synchronized (this.nativeConnectionClients) {
            if (!this.nativeConnectionClients.containsKey(hostName)) {
                T nativeConnectionClient = createConnectionClient(hostName, port);
                if (nativeConnectionClient instanceof InitializingBean) {
                    ((InitializingBean) nativeConnectionClient).afterPropertiesSet();
                }
                this.nativeConnectionClients.put(hostName, nativeConnectionClient);
            }
            return this.nativeConnectionClients.get(hostName);
        }
    }

    protected abstract T createConnectionClient(String hostName, int port) throws IOException;

    @SuppressWarnings("UnusedParameters")
    protected int getExpiryTime(String cacheName) {
        if (this.expiryTimePerCache.containsKey(cacheName) && this.expiryTimePerCache.get(cacheName) != null
                && this.expiryTimePerCache.get(cacheName) != 0) {
            return this.expiryTimePerCache.get(cacheName);
        }
        return getExpiryTime();
    }

    protected int getExpiryTime() {
        return this.expiryTime;
    }

    public void setExpiryTime(int expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setExpiryTimePerCache(Map<String, Integer> expiryTimePerCache) {
        this.expiryTimePerCache.putAll(expiryTimePerCache);
    }

}

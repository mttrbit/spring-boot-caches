package spring.caches.backend.elasticache.engines;

import org.springframework.beans.factory.InitializingBean;
import spring.caches.backend.elasticache.ElastiCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <T> connection client type
 */
public abstract class AbstractCacheFactory<T> implements CacheFactory {

    private final Map<String, T> nativeConnectionClients = new HashMap<>();

    private final Map<String, ElastiCache> settings = new HashMap<>();

    protected AbstractCacheFactory() {
    }

    protected AbstractCacheFactory(Map<String, ElastiCache> settings) {
        this.settings.putAll(settings);
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
        return settings.get(cacheName).expiration();
    }

    protected ElastiCache getSettingsPerCache(String cacheName) {
        return settings.get(cacheName);
    }

}

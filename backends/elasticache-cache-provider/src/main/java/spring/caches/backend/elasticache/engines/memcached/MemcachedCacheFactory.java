package spring.caches.backend.elasticache.engines.memcached;

import net.spy.memcached.MemcachedClient;
import spring.caches.backend.elasticache.ElastiCache;
import spring.caches.backend.elasticache.engines.AbstractCacheFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * tbd.
 */
public class MemcachedCacheFactory extends AbstractCacheFactory<MemcachedClient> {

    public MemcachedCacheFactory() {
    }

    public MemcachedCacheFactory(Map<String, ElastiCache> settings) {
        super(settings);
    }

    @Override
    public boolean isSupportingCacheArchitecture(String architecture) {
        return "memcached".equals(architecture);
    }

    @Override
    public MemcachedCache createCache(String cacheName, String host, int port) throws Exception {
        return new MemcachedCache(getConnectionFactory(host, port), cacheName, getSettingsPerCache(cacheName));
    }

    @Override
    protected MemcachedClient createConnectionClient(String hostName, int port) throws IOException {
        return new MemcachedClient(new InetSocketAddress(hostName, port));
    }

    @Override
    protected void destroyConnectionClient(MemcachedClient connectionClient) {
        connectionClient.shutdown(10, TimeUnit.SECONDS);
    }

}

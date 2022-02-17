package spring.caches.backend.elasticache.engines.memcached;

import net.spy.memcached.MemcachedClient;
import spring.caches.backend.elasticache.engines.AbstractCacheFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MemcachedCacheFactory extends AbstractCacheFactory<MemcachedClient> {

	public MemcachedCacheFactory() {
	}

	// change to Map<String, ElastiCache> settings
	public MemcachedCacheFactory(Map<String, Integer> expiryTimePerCache, int expiryTime) {
		super(expiryTimePerCache, expiryTime);
	}

	@Override
	public boolean isSupportingCacheArchitecture(String architecture) {
		return "memcached".equals(architecture);
	}

	@Override
	public MemcachedCache createCache(String cacheName, String host, int port) throws Exception {
		MemcachedCache memcachedCache = new MemcachedCache(getConnectionFactory(host, port), cacheName);
		memcachedCache.setExpiration(getExpiryTime(cacheName));
		return memcachedCache;
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

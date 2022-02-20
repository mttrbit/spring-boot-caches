package spring.caches.backend.elasticache;

import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.google.auto.service.AutoService;
import org.springframework.beans.BeansException;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.elasticache.engines.CacheFactory;
import spring.caches.backend.elasticache.engines.ElastiCacheFactory;
import spring.caches.backend.elasticache.engines.memcached.MemcachedCacheFactory;
import spring.caches.backend.elasticache.engines.redis.RedisCacheFactory;
import spring.caches.backend.properties.tree.CachesProperties;
import spring.caches.backend.properties.tree.Node;
import spring.caches.backend.properties.tree.Tree;
import spring.caches.backend.properties.tree.TreeUtils;
import spring.caches.backend.system.BackendFactory;
import spring.caches.backend.system.CacheBackendInstantiationException;
import spring.caches.backend.system.DefaultPlatform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BackendFactory for ElastiCache
 *
 * <p>When using Spring Cache's {@link DefaultPlatform}, this
 * factory will automatically be used if it is included on the classpath.
 */
@AutoService(BackendFactory.class)
public class ElastiCacheBackendFactory extends BackendFactory implements ApplicationContextAware {

    public static final String BACKEND_NAME = "elasticache";

    private ApplicationContext applicationContext;

    private static ElastiCache findSpec(Tree t) {
        return t.find(t.getRoot().getKey() + ".config.spec")
                .map(Node::getValue)
                .map(String::valueOf)
                .map(ElastiCache::from)
                .orElse(ElastiCache.newBuilder());
    }

    private static String resolveName(Tree t) {
        return t.apply(TreeUtils.valueOf(t.getRoot().getKey() + ".name"), Objects::toString);
    }

    private static List<Tree> findClusters(Tree t) {
        return t
                .find(BACKEND_NAME)
                .map(n -> TreeUtils.subtrees(node -> node.getKey().startsWith("clusters")).apply(Tree.of(n)))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public CacheBackend create(CachesProperties properties) {
        Map<String, ElastiCache> settings = new ConcurrentHashMap<>(16);
        properties.consume(t -> {
            for (Tree cluster : findClusters(t)) {
                String name = resolveName(cluster);
                ElastiCache config = findSpec(cluster);
                settings.put(name, config);

            }
        });

        if (settings.isEmpty()) {
            throw new CacheBackendInstantiationException("Invalid cache backend configuration!");
        }

        List<CacheFactory> cacheFactories = resolveCacheFactories(settings);
        List<Cache> caches = new ArrayList<>(settings.keySet().size());
        for (String cacheName : settings.keySet()) {
            caches.add(clusterCache(cacheName, cacheFactories));
        }

        return ElastiCacheBackend.of(caches);
    }

    protected Cache clusterCache(String cacheName, List<CacheFactory> cacheFactories) {
        try {
            ElastiCacheFactory factory = new ElastiCacheFactory(resolveAmazonElastiCache(), cacheName, cacheFactories);
            return factory.createInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating cache", e);
        }
    }

    @Override
    public String toString() {
        return BACKEND_NAME;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Assert.notNull(applicationContext, "applicationContext parameter is mandatory");
        this.applicationContext = applicationContext;
    }

    private AmazonElastiCache resolveAmazonElastiCache() {
        String[] names = applicationContext.getBeanNamesForType(AmazonElastiCache.class);
        Assert.notEmpty(names, "Bean of type AmazonElastiCache is mandatory");
        return applicationContext.getBean(names[0], AmazonElastiCache.class);
    }

    private List<CacheFactory> resolveCacheFactories(Map<String, ElastiCache> settings) {
        List<CacheFactory> cacheFactories = new LinkedList<>();

        cacheFactories.add(new RedisCacheFactory(settings));
        cacheFactories.add(new MemcachedCacheFactory(settings));

        return cacheFactories;
    }
}

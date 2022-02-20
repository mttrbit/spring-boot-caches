package spring.caches.backend.elasticache.engines.memcached;

import net.spy.memcached.MemcachedClientIF;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;
import spring.caches.backend.elasticache.ElastiCache;
import spring.caches.backend.elasticache.engines.memcached.stats.CacheStats;
import spring.caches.backend.elasticache.engines.memcached.stats.StatsCounter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * tbd.
 */
@SuppressWarnings("MultipleStringLiterals")
public class MemcachedCache implements Cache {

    private final MemcachedClientIF memcachedClientIF;

    private final String cacheName;
    private final boolean isRecordingStats;
    private final StatsCounter statsCounter;
    private int expiration;

    public MemcachedCache(MemcachedClientIF memcachedClientIF, String cacheName) {
        this(memcachedClientIF, cacheName, 60, false, StatsCounter.disabledStatsCounter());
    }

    public MemcachedCache(
            MemcachedClientIF memcachedClientIF,
            String cacheName,
            ElastiCache setings
    ) {
        Assert.notNull(memcachedClientIF, "memcachedClient is mandatory");
        Assert.notNull(cacheName, "cacheName is mandatory");
        this.memcachedClientIF = memcachedClientIF;
        this.cacheName = cacheName;
        this.isRecordingStats = setings.isRecordingStats();
        this.statsCounter = setings.statsCounter();
        this.expiration = setings.expiration();
    }

    public MemcachedCache(
            MemcachedClientIF memcachedClientIF,
            String cacheName,
            int expiration,
            boolean isRecordingStats,
            StatsCounter statsCounter
    ) {
        Assert.notNull(memcachedClientIF, "memcachedClient is mandatory");
        Assert.notNull(cacheName, "cacheName is mandatory");
        this.memcachedClientIF = memcachedClientIF;
        this.cacheName = cacheName;
        this.isRecordingStats = isRecordingStats;
        this.statsCounter = statsCounter;
        this.expiration = expiration;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this.memcachedClientIF;
    }

    protected Object lookup(Object key) {
        Assert.notNull(key, "key parameter is mandatory");
        // Assert.isAssignable(String.class, key.getClass());
        Object value = this.memcachedClientIF.get(String.valueOf(key));

        if (isRecordingStats) {
            if (value != null) {
                statsCounter.recordHits(1);
            } else {
                statsCounter.recordMisses(1);
            }
        }

        return value;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object result = lookup(key);
        return result != null ? new SimpleValueWrapper(result) : null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object result = lookup(key);
        if (result == null) {
            return null;
        }
        Assert.isAssignable(type, result.getClass());
        return type.cast(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper valueWrapper = get(key);
        if (valueWrapper != null) {
            return (T) valueWrapper.get();
        } else {
            T newValue = statsAware(() -> {
                try {
                    return valueLoader.call();
                } catch (Throwable ex) {
                    throw new ValueRetrievalException(key, valueLoader, ex);
                }
            }).get();

            put(key, newValue);
            return newValue;
        }
    }

    /**
     * Decorates the remapping function to record statistics if enabled.
     */
    <T, R> Supplier<? extends R> statsAware(Supplier<? extends R> mappingFunction) {
        if (!isRecordingStats) {
            return mappingFunction;
        }
        return () -> {
            statsCounter.recordMisses(1);
            return mappingFunction.get();
        };
    }

    @Override
    public void put(Object key, Object value) {
        Assert.notNull(key, "key parameter is mandatory");
        // Assert.isAssignable(String.class, key.getClass());
        try {
            this.memcachedClientIF.set(String.valueOf(key), this.expiration, value).get();
            if (isRecordingStats) {
                statsCounter.recordLoads(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Error writing key" + key, e);
        }
    }

    /**
     * <b>IMPORTANT:</b> This operation is not atomic as the underlying implementation
     * (memcached) does not provide a way to do it.
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Assert.notNull(key, "key parameter is mandatory");
        Assert.isAssignable(String.class, key.getClass());

        ValueWrapper valueWrapper = get(key);
        if (valueWrapper == null) {
            try {
                this.memcachedClientIF.add((String) key, this.expiration, value).get();
                if (isRecordingStats) {
                    statsCounter.recordLoads(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new IllegalArgumentException("Error writing key" + key, e);
            }
            return null;
        } else {
            return valueWrapper;
        }
    }

    @Override
    public void evict(Object key) {
        Assert.notNull(key, "key parameter is mandatory");
        Assert.isAssignable(String.class, key.getClass());
        try {
            this.memcachedClientIF.delete((String) key).get();
            if (isRecordingStats) {
                statsCounter.recordEviction(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Error evicting items" + key);
        }
    }

    @Override
    public void clear() {
        this.memcachedClientIF.flush();
    }

    public CacheStats stats() {
        return statsCounter.snapshot();
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

}

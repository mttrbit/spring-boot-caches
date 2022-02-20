package spring.caches.backend.simple;

import spring.caches.backend.simple.stats.CacheStats;
import spring.caches.backend.simple.stats.StatsCounter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

class UnboundedSimpleCache extends ConcurrentHashMap<Object, Object> implements Cache {

    private final StatsCounter statsCounter;
    private final boolean isRecordingStats;

    UnboundedSimpleCache(Simple builder) {
        super(builder.getInitialCapacity());
        statsCounter = builder.getStatsCounterSupplier().get();
        isRecordingStats = builder.isRecordingStats();
    }

    @Override
    public Object get(Object key) {
        Object value = super.get(key);

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
    public Object get(Object key, Callable<Object> valueLoader) {
        return computeIfAbsent(key, k -> {
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new org.springframework.cache.Cache.ValueRetrievalException(key, valueLoader, ex);
            }
        });
    }

    @Override
    public Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        if (isRecordingStats && containsKey(key)) {
            statsCounter.recordHits(1);
        }
        return super.computeIfAbsent(key, statsAware(mappingFunction));
    }

    @Override
    public Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (isRecordingStats && containsKey(key)) {
            statsCounter.recordHits(1);
        }
        return super.compute(key, statsAware(remappingFunction));
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        if (isRecordingStats && !containsKey(key)) {
            statsCounter.recordLoads(1);
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    public Object put(Object key, Object value) {
        if (isRecordingStats) {
            statsCounter.recordLoads(1);
        }

        return super.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        Object value = super.remove(key);
        if (isRecordingStats && value != null) {
            statsCounter.recordEviction(1);
        }
        return value;
    }

    @Override
    public void putAll(Map<?, ?> m) {
        int size = size();
        if (isRecordingStats && size > 0) {
            statsCounter.recordLoads(size);
        }
        super.putAll(m);
    }

    @Override
    public CacheStats stats() {
        return statsCounter.snapshot();
    }

    @Override
    public long estimatedSize() {
        return size();
    }

    @Override
    public ConcurrentMap<Object, Object> asMap() {
        return this;
    }

    @Override
    public void invalidate(Object key) {
        remove(key);
    }

    @Override
    public void invalidateAll() {
        for (Object key : keySet()) {
            remove(key);
        }
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        for (Object key : keys) {
            remove(key);
        }
    }

    /**
     * Decorates the remapping function to record statistics if enabled.
     */
    <T, R> Function<? super T, ? extends R> statsAware(Function<? super T, ? extends R> mappingFunction) {
        if (!isRecordingStats) {
            return mappingFunction;
        }
        return key -> {
            statsCounter.recordMisses(1);
            return mappingFunction.apply(key);
        };
    }

    /**
     * Decorates the remapping function to record statistics if enabled.
     */
    <K, V> BiFunction<? super K, ? super V, ? extends V> statsAware(
            BiFunction<? super K, ? super V, ? extends V> mappingFunction
    ) {
        if (!isRecordingStats) {
            return mappingFunction;
        }
        return (k, v) -> {
            statsCounter.recordMisses(1);
            return mappingFunction.apply(k, v);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UnboundedSimpleCache that = (UnboundedSimpleCache) o;
        return isRecordingStats == that.isRecordingStats && statsCounter.equals(that.statsCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statsCounter, isRecordingStats);
    }
}

package spring.caches.backend.elasticache.engines.redis;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import org.springframework.data.redis.cache.RedisCache;

import java.util.concurrent.TimeUnit;

/**
 * TODO delete this class when upgrading to latest version of spring boot actuator.
 * <p>
 * {@link CacheMeterBinder} for {@link RedisCache}.
 *
 * @deprecated delete this class once the project is updated to latest version of spring boot actuator.
 */
@Deprecated
public class RedisCacheMetrics extends CacheMeterBinder {

    private final RedisCache cache;

    public RedisCacheMetrics(RedisCache cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
        this.cache = cache;
    }

    @Override
    protected Long size() {
        return null;
    }

    @Override
    protected long hitCount() {
        return this.cache.getStatistics().getHits();
    }

    @Override
    protected Long missCount() {
        return this.cache.getStatistics().getMisses();
    }

    @Override
    protected Long evictionCount() {
        return null;
    }

    @Override
    protected long putCount() {
        return this.cache.getStatistics().getPuts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        FunctionCounter.builder("cache.removals", this.cache, (cache) -> cache.getStatistics().getDeletes())
                .tags(getTagsWithCacheName()).description("Cache removals").register(registry);
        FunctionCounter.builder("cache.gets", this.cache, (cache) -> cache.getStatistics().getPending())
                .tags(getTagsWithCacheName()).tag("result", "pending").description("The number of pending requests")
                .register(registry);
        TimeGauge
                .builder("cache.lock.duration", this.cache, TimeUnit.NANOSECONDS,
                        (cache) -> cache.getStatistics().getLockWaitDuration(TimeUnit.NANOSECONDS))
                .tags(getTagsWithCacheName()).description("The time the cache has spent waiting on a lock")
                .register(registry);
    }

}

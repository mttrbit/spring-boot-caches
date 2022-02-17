package spring.caches.backend.elasticache.engines.redis;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.data.redis.cache.RedisCache;

/**
 * TODO delete this class when upgrading to latest version of spring boot actuator.
 *
 * {@link CacheMeterBinderProvider} implementation for Redis.
 *
 * @author Stephane Nicoll
 * @since 2.4.0
 */
@Deprecated
public class RedisCacheMeterBinderProvider implements CacheMeterBinderProvider<RedisCache> {

    @Override
    public MeterBinder getMeterBinder(RedisCache cache, Iterable<Tag> tags) {
        return new RedisCacheMetrics(cache, tags);
    }

}
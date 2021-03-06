/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spring.caches.backend.elasticache.engines.redis;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.ClassUtils;
import spring.caches.backend.elasticache.ElastiCache;
import spring.caches.backend.elasticache.engines.AbstractCacheFactory;

import java.util.Map;

/**
 * @author Agim Emruli
 */
public class RedisCacheFactory extends AbstractCacheFactory<RedisConnectionFactory> {

    private static final boolean JEDIS_AVAILABLE = ClassUtils.isPresent("redis.clients.jedis.Jedis",
            ClassUtils.getDefaultClassLoader());

    private static final boolean LETTUCE_AVAILABLE = ClassUtils.isPresent("io.lettuce.core.RedisClient",
            ClassUtils.getDefaultClassLoader());

    public RedisCacheFactory() {
    }

    // change to Map<String, ElastiCache> settings
    public RedisCacheFactory(Map<String, ElastiCache> settings) {
        super(settings);
    }

    @Override
    public boolean isSupportingCacheArchitecture(String architecture) {
        return "redis".equalsIgnoreCase(architecture);
    }

    @Override
    public Cache createCache(String cacheName, String host, int port) throws Exception {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(getConnectionFactory(host, port));

        if (getSettingsPerCache(cacheName).isRecordingStats()) {
            builder.enableStatistics();
        }

        return builder.build().getCache(cacheName);
    }

    @Override
    protected void destroyConnectionClient(RedisConnectionFactory connectionClient) throws Exception {

    }

    @Override
    protected RedisConnectionFactory createConnectionClient(String hostName, int port) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(hostName);
        configuration.setPort(port);
        if (JEDIS_AVAILABLE) {
            return new JedisConnectionFactory(configuration);
        } else if (LETTUCE_AVAILABLE) {
            return new LettuceConnectionFactory(configuration);
        } else {
            throw new IllegalArgumentException("No Jedis or lettuce client on classpath. "
                    + "Please add one of the implementation to your classpath");
        }
    }

}

package spring.caches.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CachesAutoConfiguration}.
 */
class CachesAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CachesAutoConfiguration.class))
            .withSystemProperties("cache.backend_factories=spring.caches.backend.system.DefaultBackendFactory#getInstance")
            .withBean("cacheableMethod", CacheableMethod.class);

    static class CacheableMethod {
        private static final Random random = new Random();

        @Cacheable(cacheNames = "cache1")
        public int computeInt(int a) {
            return random.nextInt();
        }
    }

    @Test
    void runWithDefaultPropertyShouldHaveCacheManagerAndCache() {
        this.contextRunner.withPropertyValues("spring.caches.default.names=cache1")
                .run(context -> assertThat(context).hasBean("defaultCacheManager"))
                .run(context -> {
                    CacheManager cacheManager = context.getBean("defaultCacheManager", CacheManager.class);
                    assertThat(cacheManager.getCache("cache1")).isNotNull();
                    assertThat(cacheManager.getCache("cache2")).isNull();

                    CacheableMethod method = context.getBean("cacheableMethod", CacheableMethod.class);
                    assertThat(method.computeInt(9)).isEqualTo(method.computeInt(9)).isEqualTo(method.computeInt(9));
                });
    }
}
package spring.caches.backend.simple;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.annotation.Cacheable;
import spring.caches.autoconfigure.CachesAutoConfiguration;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringCacheManagerIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CachesAutoConfiguration.class))
            .withPropertyValues(
                    "spring.caches.simple[0].names=test",
                    "spring.caches.simple[0].config.spec=recordStats"
            )
            .withBean("cacheableMethod", CacheableMethod.class);

    @Test
    void runWithPropertiesShouldLoadSimpleCacheManager() {
        this.contextRunner
                .run(context -> assertThat(context).hasBean("simpleCacheManager"))
                .run(context -> {
                    SimpleCacheManager cacheManager = context.getBean("simpleCacheManager", SimpleCacheManager.class);
                    assertThat(cacheManager.getCache("test")).isNotNull();
                    assertThat(cacheManager.getCache("test")).isInstanceOf(SimpleCache.class);
                    CacheableMethod method = context.getBean("cacheableMethod", CacheableMethod.class);
                    assertThat(method.computeInt(9)).isEqualTo(method.computeInt(9)).isEqualTo(method.computeInt(9));
                });
    }

    static class CacheableMethod {
        private static final Random random = new Random();

        @Cacheable(cacheNames = "test")
        public int computeInt(int ignored) {
            return random.nextInt();
        }
    }

}

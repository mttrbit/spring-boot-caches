package spring.caches.backend.caffeine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.annotation.Cacheable;
import spring.caches.autoconfigure.CachesAutoConfiguration;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringCacheManagerIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CachesAutoConfiguration.class));

    @Test
    void runWithPropertiesShouldLoadCaffeineCacheManager() {
        this.contextRunner
                .withPropertyValues(
                        "spring.caches.caffeine[0].names=cache1",
                        "spring.caches.caffeine[0].config.spec=maximumSize=500,expireAfterAccess=600s",
                        "spring.caches.caffeine[1].names=cache2",
                        "spring.caches.caffeine[1].config.spec=maximumSize=50,expireAfterAccess=10s"
                )
                .withBean("cacheableMethod", CacheableMethod.class)
                .run(context -> {
                    assertThat(context).hasBean("caffeineCacheManager");
                    CaffeineCacheManager cacheManager = context.getBean("caffeineCacheManager", CaffeineCacheManager.class);
                    assertThat(cacheManager.getCache("cache1")).isNotNull();
                    assertThat(cacheManager.getCache("cache2")).isNotNull();
                    assertThat(cacheManager.getCache("cache1")).isInstanceOf(CaffeineCache.class);
                    CacheableMethod method = context.getBean("cacheableMethod", CacheableMethod.class);
                    assertThat(method.computeInt(9)).isEqualTo(method.computeInt(9)).isEqualTo(method.computeInt(9));
                });
    }

    @Test
    void runWithProperties_invalidConfiguration_missingProperties() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean("caffeineCacheManager"));
    }

    static class CacheableMethod {
        private static final Random random = new Random();

        @Cacheable(cacheNames = "cache1")
        public int computeInt(int a) {
            return random.nextInt();
        }
    }

}

package spring.caches.backend.metrics.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import spring.caches.autoconfigure.CachesAutoConfiguration;
import spring.caches.metrics.autoconfigure.CachesMetricsAutoConfiguration;

public class CacheMetricsAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CachesAutoConfiguration.class,
                    CachesMetricsAutoConfiguration.class,
                    MetricsAutoConfiguration.class,
                    CompositeMeterRegistryAutoConfiguration.class
            ));


    @Test
    void autoConfiguredCacheManagerIsInstrumented() {
        this.contextRunner.withPropertyValues(
                        "spring.caches.simple[0].names=simple",
                        "spring.caches.simple[0].config.spec=initialCapacity=32,recordStats"
                )
                .run((context) -> {
                    MeterRegistry registry = context.getBean(MeterRegistry.class);
                    registry.get("cache.gets").tags("name", "simple").tags("cacheManager", "simple").meter();
                });
    }
}

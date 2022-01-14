package spring.caches.metrics.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.caches.autoconfigure.CachesAutoConfiguration;

/**
 * Binds {@link CacheMetricsRegistrar} to .
 * Allows configuring metrics for dynamic caches.
 * Partially duplicates functionality from Spring Boot's package-private class
 * {@code org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsRegistrarConfiguration}.
 */
@ConditionalOnClass(MetricsAutoConfiguration.class)
@AutoConfigureAfter({CachesAutoConfiguration.class, MetricsAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
@Import(CachesMetricsRegistrarConfiguration.class)
public class CachesMetricsAutoConfiguration {

}

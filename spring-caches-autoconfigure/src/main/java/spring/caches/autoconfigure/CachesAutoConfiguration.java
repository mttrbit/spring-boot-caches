package spring.caches.autoconfigure;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the caches abstraction. Creates a
 * map of {@link CacheManager cache managers} if necessary when caching is enabled via
 * {@link EnableCaching @EnableCaching}.
 * <p>
 * CacheManagers can be auto-detected or specified explicitly via configuration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CacheManager.class)
@ConditionalOnMissingBean(name = {"cacheManagers", "springCachesResolver"})
@Import({CachesConfiguration.class, CacheResolverConfiguration.class})
public class CachesAutoConfiguration {

}

package spring.caches.autoconfigure;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import com.amazonaws.services.elasticache.model.Endpoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;

import static io.awspring.cloud.core.config.AmazonWebserviceClientConfigurationUtils.GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CachesElastiCacheAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CachesElastiCacheAutoConfiguration.class));

    @Test
    void cacheManager_configuredNoCachesWithNoStack_configuresNoCacheManager() {
        this.contextRunner.run(context -> {
            assertThat(context.containsBean("elasticacheCacheManager")).isFalse();
        });
    }

    @Test
    void cacheManager_configuredMultipleCaches_configuresCacheManager() {
        this.contextRunner
                .withConfiguration(AutoConfigurations.of(CachesAutoConfiguration.class))
                .withPropertyValues(
                        "spring.caches.elasticache.clusters[0].name=sampleCacheOneLogical",
                        "spring.caches.elasticache.clusters[0].config.spec=recordStats,expiration=100",
                        "spring.caches.elasticache.clusters[1].name=sampleCacheTwoLogical",
                        "spring.caches.elasticache.clusters[1].config.spec=recordStats,expiration=600"
                )
                .withUserConfiguration(MockCacheConfiguration.class).run(context -> {
                    CacheManager cacheManager = context.getBean("elasticacheCacheManager", CacheManager.class);
                    assertThat(cacheManager.getCacheNames().contains("sampleCacheOneLogical")).isTrue();
                    assertThat(cacheManager.getCacheNames().contains("sampleCacheTwoLogical")).isTrue();
                    assertThat(cacheManager.getCacheNames()).hasSize(2);
                });
    }

    @Test
    void configuration_withGlobalClientConfiguration_shouldUseItForClient() {
        // Arrange & Act
        this.contextRunner.withUserConfiguration(ConfigurationWithGlobalClientConfiguration.class).run((context) -> {
            AmazonElastiCache client = context.getBean(AmazonElastiCache.class);

            // Assert
            ClientConfiguration clientConfiguration = (ClientConfiguration) ReflectionTestUtils.getField(client,
                    "clientConfiguration");
            assertThat(clientConfiguration.getProxyHost()).isEqualTo("global");
        });
    }

    @Test
    void configuration_withElastiCacheClientConfiguration_shouldUseItForClient() {
        // Arrange & Act
        this.contextRunner
                .withUserConfiguration(ConfigurationWithElastiCacheClientConfiguration.class)
                .run((context) -> {
                    AmazonElastiCache client = context.getBean(AmazonElastiCache.class);

                    // Assert
                    ClientConfiguration clientConfiguration = (ClientConfiguration) ReflectionTestUtils.getField(client,
                            "clientConfiguration");
                    assertThat(clientConfiguration.getProxyHost()).isEqualTo("elastiCache");
                });
    }

    @Test
    void configuration_withGlobalAndElastiCacheClientConfigurations_shouldUseSqsConfigurationForClient() {
        // Arrange & Act
        this.contextRunner.withUserConfiguration(ConfigurationWithGlobalAndElastiCacheClientConfiguration.class)
                .run((context) -> {
                    AmazonElastiCache client = context.getBean(AmazonElastiCache.class);

                    // Assert
                    ClientConfiguration clientConfiguration = (ClientConfiguration) ReflectionTestUtils.getField(client,
                            "clientConfiguration");
                    assertThat(clientConfiguration.getProxyHost()).isEqualTo("elastiCache");
                });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MockCacheConfiguration {

        @Bean
        AmazonElastiCache amazonElastiCache() {
            AmazonElastiCache amazonElastiCache = mock(AmazonElastiCache.class);
            int port = TestMemcacheServer.startServer();
            DescribeCacheClustersRequest sampleCacheOneLogical = new DescribeCacheClustersRequest()
                    .withCacheClusterId("sampleCacheOneLogical");
            sampleCacheOneLogical.setShowCacheNodeInfo(true);

            Mockito.when(amazonElastiCache.describeCacheClusters(sampleCacheOneLogical))
                    .thenReturn(new DescribeCacheClustersResult().withCacheClusters(new CacheCluster()
                            .withConfigurationEndpoint(new Endpoint().withAddress("localhost").withPort(port))
                            .withEngine("memcached")));

            DescribeCacheClustersRequest sampleCacheTwoLogical = new DescribeCacheClustersRequest()
                    .withCacheClusterId("sampleCacheTwoLogical");
            sampleCacheTwoLogical.setShowCacheNodeInfo(true);

            Mockito.when(amazonElastiCache.describeCacheClusters(sampleCacheTwoLogical))
                    .thenReturn(new DescribeCacheClustersResult().withCacheClusters(new CacheCluster()
                            .withConfigurationEndpoint(new Endpoint().withAddress("localhost").withPort(port))
                            .withEngine("memcached")));
            return amazonElastiCache;
        }

    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfigurationWithGlobalClientConfiguration {

        @Bean(name = GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME)
        ClientConfiguration globalClientConfiguration() {
            return new ClientConfiguration().withProxyHost("global");
        }

    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfigurationWithElastiCacheClientConfiguration {

        @Bean
        ClientConfiguration elastiCacheClientConfiguration() {
            return new ClientConfiguration().withProxyHost("elastiCache");
        }

    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfigurationWithGlobalAndElastiCacheClientConfiguration {

        @Bean
        ClientConfiguration elastiCacheClientConfiguration() {
            return new ClientConfiguration().withProxyHost("elastiCache");
        }

        @Bean(name = GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME)
        ClientConfiguration globalClientConfiguration() {
            return new ClientConfiguration().withProxyHost("global");
        }

    }

}
package spring.caches.autoconfigure;

import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import com.amazonaws.services.elasticache.model.Endpoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = MemcachedWithStatisticsEndpointTest.PropertiesInitializer.class)
class MemcachedWithStatisticsEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MockConfiguration.CacheableMethod cacheableMethod;

    @Test
    void cacheEndpoint_noStatistics() {
        assertThat(cacheableMethod.computeInt(1)).isEqualTo(cacheableMethod.computeInt(1));
        assertThat(cacheableMethod.computeInt(4)).isNotEqualTo(cacheableMethod.computeInt(6));

        var response = webTestClient.get()
                .uri("/actuator/metrics/cache.gets?tag=cache:a-cache&tag=cacheManager:elasticache&tag=name:a-cache&tag=result:miss")
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("name").isEqualTo("cache.gets")
                .jsonPath("measurements[0].statistic").isEqualTo("COUNT")
                .jsonPath("measurements[0].value").isEqualTo("0.0");
    }

    @Test
    void cacheEndpoint_withStatistics_count_misses() {
        assertThat(cacheableMethod.computeInt(1)).isEqualTo(cacheableMethod.computeInt(1));
        assertThat(cacheableMethod.computeInt(4)).isNotEqualTo(cacheableMethod.computeInt(6));

        webTestClient.get()
                .uri("/actuator/metrics/cache.gets?tag=cache:test-cache&tag=cacheManager:elasticache&tag=name:test-cache&tag=result:miss")
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("name").isEqualTo("cache.gets")
                .jsonPath("measurements[0].statistic").isEqualTo("COUNT")
                .jsonPath("measurements[0].value").isEqualTo("3.0");
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.caches.elasticache.clusters[0].name=test-cache",
                    "spring.caches.elasticache.clusters[0].config.spec=recordStats,expiration=100",
                    "spring.caches.elasticache.clusters[1].name=a-cache",
                    "spring.caches.elasticache.clusters[1].config.spec=expiration=100",
                    "management.endpoints.web.exposure.include=metrics"
            ).applyTo(applicationContext);
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class MockConfiguration {

        @Bean
        AmazonElastiCache amazonElastiCache() {
            AmazonElastiCache amazonElastiCache = mock(AmazonElastiCache.class);
            int port = TestMemcacheServer.startServer();
            DescribeCacheClustersRequest sampleCacheOneLogical = new DescribeCacheClustersRequest()
                    .withCacheClusterId("a-cache");
            sampleCacheOneLogical.setShowCacheNodeInfo(true);

            Mockito.when(amazonElastiCache.describeCacheClusters(sampleCacheOneLogical))
                    .thenReturn(new DescribeCacheClustersResult().withCacheClusters(new CacheCluster()
                            .withConfigurationEndpoint(new Endpoint().withAddress("localhost").withPort(port))
                            .withEngine("memcached")));

            DescribeCacheClustersRequest sampleCacheTwoLogical = new DescribeCacheClustersRequest()
                    .withCacheClusterId("test-cache");
            sampleCacheTwoLogical.setShowCacheNodeInfo(true);

            Mockito.when(amazonElastiCache.describeCacheClusters(sampleCacheTwoLogical))
                    .thenReturn(new DescribeCacheClustersResult().withCacheClusters(new CacheCluster()
                            .withConfigurationEndpoint(new Endpoint().withAddress("localhost").withPort(port))
                            .withEngine("memcached")));
            return amazonElastiCache;
        }

        @Bean
        public CacheableMethod cacheableMethod() {
            return new CacheableMethod();
        }

        // Added for testing purposes only
        public static class CacheableMethod {
            private static final Random random = new Random();

            @Cacheable(cacheNames = {"test-cache", "a-cache"})
            public int computeInt(int a) {
                return random.nextInt();
            }
        }
    }

}

package spring.caches.metrics.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Random;

@SpringBootApplication(exclude = {CacheAutoConfiguration.class, CacheMetricsAutoConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.caches.caffeine[0].names=name1, name2, name3",
        "spring.caches.caffeine[0].config.spec=maximumSize=500,expireAfterAccess=600s",
        "spring.caches.caffeine[1].names=coffee_cache",
        "spring.caches.caffeine[1].config.spec=maximumSize=100,expireAfterAccess=10s,recordStats",
        "management.port=0",
        "management.endpoints.web.exposure.include=*"
})
public class CachesEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    TestConfig.CacheableMethod cacheableMethod;

    @Value("${local.management.port}")
    private int mgt;

    @Test
    void allCaches() {
        System.out.println(cacheableMethod.computeInt(1));
        System.out.println(cacheableMethod.computeInt(4));
        System.out.println(cacheableMethod.computeInt(1));
        System.out.println(cacheableMethod.computeInt(6));
        var response = webTestClient.get()
                .uri("http://localhost:" + this.mgt + "/actuator/metrics/cache.gets?tag=cache:coffee_cache&tag=cacheManager:caffeine&tag=name:coffee_cache&tag=result:miss")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).returnResult()
                .getResponseBody();

        System.out.println(response);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestConfig.CacheableMethod cacheableMethod() {
            return new TestConfig.CacheableMethod();
        }

        // Added for testing purposes only
        public static class CacheableMethod {
            private static final Random random = new Random();

            @Cacheable(cacheNames = "coffee_cache")
            public int computeInt(int a) {
                return random.nextInt();
            }
        }
    }

}

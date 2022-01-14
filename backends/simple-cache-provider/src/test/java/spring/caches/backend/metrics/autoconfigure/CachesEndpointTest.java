package spring.caches.backend.metrics.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        "spring.caches.simple[0].names=simple",
        "spring.caches.simple[0].config.spec=initialCapacity=500,recordStats",
        "management.endpoints.web.exposure.include=*"
})
public class CachesEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    TestConfig.CacheableMethod cacheableMethod;

    @Test
    void allCaches() {
        System.out.println(cacheableMethod.computeInt(1));
        System.out.println(cacheableMethod.computeInt(4));
        System.out.println(cacheableMethod.computeInt(1));
        System.out.println(cacheableMethod.computeInt(6));
        var response = webTestClient.get()
                .uri("/actuator/metrics/cache.gets?tag=cache:simple&tag=cacheManager:simple&tag=name:simple&tag=result:miss")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody().jsonPath("measurements[0].value").isEqualTo(3.0);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CacheableMethod cacheableMethod() {
            return new CacheableMethod();
        }

        // Added for testing purposes only
        public static class CacheableMethod {
            private static final Random random = new Random();

            @Cacheable(cacheNames = "simple")
            public int computeInt(int a) {
                return random.nextInt();
            }
        }
    }

}

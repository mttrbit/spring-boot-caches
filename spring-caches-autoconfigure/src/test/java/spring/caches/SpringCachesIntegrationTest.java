package spring.caches;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.caches.Caffeine[0].names=name1, name2, name3",
        "spring.caches.Caffeine[0].config.spec=maximumSize=500,expireAfterAccess=600s",
        "spring.caches.Caffeine[1].names=coffee_cache",
        "spring.caches.Caffeine[1].config.spec=maximumSize=100,expireAfterAccess=1s",
})
public class SpringCachesIntegrationTest {

//    @Autowired
//    TestConfig.CacheableMethod cacheableMethod;
//
//    @Test
//    public void load_context() {
//        assertThat(cacheableMethod.computeInt()).isEqualTo(cacheableMethod.computeInt());
//    }
//
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        public CacheableMethod cacheableMethod() {
//            return new CacheableMethod();
//        }
//
//        // Added for testing purposes only
//        @Cacheable(cacheNames = "coffee_cache", cacheResolver = "cacheResolver")
//        public static class CacheableMethod {
//            private static final Random random = new Random();
//
//            public int computeInt() {
//                return random.nextInt();
//            }
//        }
//    }
}

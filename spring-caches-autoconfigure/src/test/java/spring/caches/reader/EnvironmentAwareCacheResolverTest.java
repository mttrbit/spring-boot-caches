package spring.caches.reader;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class})
@TestPropertySource(properties = {
        "smartlife.caches.coffee_cache.type=Caffeine",
        "smartlife.caches.coffee_cache.specs=key=1",
        "smartlife.caches.redis_cache.type=Redis",
        "smartlife.caches.redis_cache.expiration-time=Sun Aug 20 02:00:00 CEST 2023"
})
class EnvironmentAwareCacheResolverTest {

//    @Autowired
//    private EnvironmentAwareCacheResolver environmentAwareCacheResolver;
//
//    @Test
//    public void load_context() {
//
//    }
//
//    @Test
//    public void load_properties() {
//        assertThat(environmentAwareCacheResolver.read().isEmpty()).isFalse();
//    }

}
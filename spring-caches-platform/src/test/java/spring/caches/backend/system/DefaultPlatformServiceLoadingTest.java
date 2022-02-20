package spring.caches.backend.system;

import com.google.auto.service.AutoService;
import org.junit.jupiter.api.Test;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.CachesProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests how {@code DefaultPlatform} loads services from the classpath.
 */
public class DefaultPlatformServiceLoadingTest {
    @Test
    public void testConfigString() {
        DefaultPlatform platform = new DefaultPlatform();
        assertThat(platform.getConfigInfoImpl()).contains(DefaultPlatform.class.getName());
        assertThat(platform.getConfigInfoImpl()).contains("BackendFactories: [TestBackendFactoryService]");
    }

    @AutoService(BackendFactory.class)
    public static final class TestBackendFactoryService extends BackendFactory {
        @Override
        public CacheBackend create(CachesProperties multiCacheProperties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "TestBackendFactoryService";
        }
    }
}

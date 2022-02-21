package spring.caches.backend.system;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.CachesProperties;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultPlatformDefaultBackendTest {

    BackendFactory backendFactory = DefaultBackendFactory.getInstance();
    private FakeDefaultPlatform platform;

    @BeforeEach
    public void initializeMocks() {
        platform = new FakeDefaultPlatform(Collections.singletonMap(DefaultBackendFactory.BACKEND_NAME, backendFactory));
    }

    @Test
    public void testBackendFactory() {
        Assertions.assertThat(platform.getBackendFactory("default").create(new CachesProperties())).isInstanceOf(CacheBackend.class);
    }

    @Test
    public void testConfigString() {
        assertThat(platform.getConfigInfoImpl()).contains(DefaultPlatform.class.getName());
        assertThat(platform.getConfigInfoImpl()).contains("BackendFactories: [default]");
    }

    private static final class FakeDefaultPlatform extends DefaultPlatform {
        FakeDefaultPlatform(Map<String, BackendFactory> backendFactories) {
            super(backendFactories);
        }
    }

}
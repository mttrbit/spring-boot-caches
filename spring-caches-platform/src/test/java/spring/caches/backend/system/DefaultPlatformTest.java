package spring.caches.backend.system;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import spring.caches.backend.CacheBackend;
import spring.caches.backend.properties.tree.MultiCacheProperties;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class DefaultPlatformTest {

    @Mock
    BackendFactory mockBackendFactory;
    private FakeDefaultPlatform platform;

    @BeforeEach
    public void initializeMocks() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(mockBackendFactory.toString()).thenReturn("Mock Backend Factory");

        platform = new FakeDefaultPlatform(Collections.singletonMap("mock", mockBackendFactory));
    }

    @Test
    public void testBackendFactory() {
        CacheBackend mockBackend = Mockito.mock(CacheBackend.class);
        Mockito.when(mockBackendFactory.create(any())).thenReturn(mockBackend);
        Assertions.assertThat(platform.getBackendFactory("mock").create(new MultiCacheProperties())).isEqualTo(mockBackend);
    }

    @Test
    public void testConfigString() {
        assertThat(platform.getConfigInfoImpl()).contains(DefaultPlatform.class.getName());
        assertThat(platform.getConfigInfoImpl()).contains("BackendFactories: [Mock Backend Factory]");
    }

    private static final class FakeDefaultPlatform extends DefaultPlatform {
        FakeDefaultPlatform(Map<String, BackendFactory> backendFactories) {
            super(backendFactories);
        }
    }

}
package spring.caches.backend.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformLoaderTest {

    @Test
    public void testConfigString() {
        DefaultPlatform platform = new DefaultPlatform();
        assertThat(platform.getConfigInfoImpl()).contains(DefaultPlatform.class.getName());
        assertThat(platform.getConfigInfoImpl()).contains("BackendFactories: [simple]");
    }

}

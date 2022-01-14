package spring.caches.reader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = Configuration.class)
@TestPropertySource(properties = {"spring.config.location=classpath:test2.yml"})
class EnvironmentAwareCacheResolverYmlTest {

    @Test
    public void load_context(@Autowired Environment environment) {
        Map<String, Object> props = resolveProperties(ConfigurationPropertySources.get(environment));

        System.out.println(props);
    }
    private Map<String, Object> resolveProperties(Iterable<ConfigurationPropertySource> sources) {
        return StreamSupport.stream(sources.spliterator(), false)
                .map(ConfigurationPropertySource::getUnderlyingSource)
                .filter(source -> source instanceof MapPropertySource)
                .filter(source -> filterSourcesByName((MapPropertySource) source, "applicationConfig: ", "Inlined Test Properties"))
                .collect(HashMap::new, (m, n) -> m.putAll(((MapPropertySource) n).getSource()), Map::putAll);
    }
    private boolean filterSourcesByName(MapPropertySource source, String... names) {
        for (String name : names) {
            if (source.getName().startsWith(name)) {
                return true;
            }
        }

        return false;
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration {

    }
}
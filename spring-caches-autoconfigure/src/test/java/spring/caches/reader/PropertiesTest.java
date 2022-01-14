package spring.caches.reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@ExtendWith({SpringExtension.class})
@TestPropertySource(properties = {
        "spring.caches.Caffeine[0].names=name1, name2, name3",
        "spring.caches.Caffeine[0].config.spec=maximumSize=500,expireAfterAccess=600s",
        "spring.caches.Caffeine[2].aaa.spec=maximumSize=500,expireAfterAccess=600s",
        "spring.caches.Caffeine[1].names=name4",
        "spring.caches.Caffeine[1].config.spec=maximumSize=100,expireAfterAccess=1s",
})
class PropertiesTest {

//    @Autowired
//    private EnvironmentAwareCacheResolver environmentAwareCacheResolver;
//
    @Test
    public void load_context(@Autowired Environment environment) {
        // Note: order is unstable
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
//
//    @Test
//    public void load_properties() {
//        assertThat(environmentAwareCacheResolver.read().isEmpty()).isFalse();
//    }

}
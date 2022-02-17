package spring.caches.autoconfigure;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import io.awspring.cloud.core.config.AmazonWebserviceClientFactoryBean;
import io.awspring.cloud.core.region.RegionProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.caches.autoconfigure.context.ContextCredentialsAutoConfiguration;

import static io.awspring.cloud.core.config.AmazonWebserviceClientConfigurationUtils.GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME;

@Import({ContextCredentialsAutoConfiguration.class})
@ConditionalOnClass(com.amazonaws.services.elasticache.AmazonElastiCache.class)
@AutoConfigureBefore({CachesAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
public class CachesElastiCacheAutoConfiguration {

    private final ClientConfiguration clientConfiguration;

    public CachesElastiCacheAutoConfiguration(
            @Qualifier(GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME) ObjectProvider<ClientConfiguration> globalClientConfiguration,
            @Qualifier("elastiCacheClientConfiguration") ObjectProvider<ClientConfiguration> elastiCacheClientConfiguration
    ) {
        this.clientConfiguration = elastiCacheClientConfiguration
                .getIfAvailable(globalClientConfiguration::getIfAvailable);
    }

    @Bean
    @ConditionalOnMissingBean(AmazonElastiCache.class)
    public AmazonWebserviceClientFactoryBean<AmazonElastiCacheClient> amazonElastiCache(
            ObjectProvider<RegionProvider> regionProvider, ObjectProvider<AWSCredentialsProvider> credentialsProvider) {
        return new AmazonWebserviceClientFactoryBean<>(AmazonElastiCacheClient.class,
                credentialsProvider.getIfAvailable(), regionProvider.getIfAvailable(), clientConfiguration);
    }
}

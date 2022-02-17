/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spring.caches.backend.elasticache.engines;

import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import com.amazonaws.services.elasticache.model.Endpoint;
import org.springframework.cache.Cache;

import java.util.List;

/**
 * @author Agim Emruli
 */
public class ElastiCacheFactory {

	private final AmazonElastiCache amazonElastiCache;

	private final String cacheClusterId;

	private final List<? extends CacheFactory> cacheFactories;

	public ElastiCacheFactory(AmazonElastiCache amazonElastiCache, String cacheClusterId, List<? extends CacheFactory> cacheFactories) {
		this.amazonElastiCache = amazonElastiCache;
		this.cacheClusterId = cacheClusterId;
		this.cacheFactories = cacheFactories;
	}

	private static Endpoint getEndpointForCache(CacheCluster cacheCluster) {
		if (cacheCluster.getConfigurationEndpoint() != null) {
			return cacheCluster.getConfigurationEndpoint();
		}

		if (!cacheCluster.getCacheNodes().isEmpty()) {
			return cacheCluster.getCacheNodes().get(0).getEndpoint();
		}

		throw new IllegalArgumentException("No Configuration Endpoint or Cache Node available to "
				+ "receive address information for cluster:'" + cacheCluster.getCacheClusterId() + "'");
	}

	public Cache createInstance() throws Exception {
		DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest()
				.withCacheClusterId(getCacheClusterName());
		describeCacheClustersRequest.setShowCacheNodeInfo(true);

		DescribeCacheClustersResult describeCacheClustersResult = this.amazonElastiCache
				.describeCacheClusters(describeCacheClustersRequest);

		CacheCluster cacheCluster = describeCacheClustersResult.getCacheClusters().get(0);
		Endpoint configurationEndpoint = getEndpointForCache(cacheCluster);

		for (CacheFactory cacheFactory : this.cacheFactories) {
			if (cacheFactory.isSupportingCacheArchitecture(cacheCluster.getEngine())) {
				return cacheFactory.createCache(this.cacheClusterId, configurationEndpoint.getAddress(),
						configurationEndpoint.getPort());
			}
		}

		throw new IllegalArgumentException("No CacheFactory configured for engine: " + cacheCluster.getEngine());
	}

	private String getCacheClusterName() {
		return this.cacheClusterId;
	}

}

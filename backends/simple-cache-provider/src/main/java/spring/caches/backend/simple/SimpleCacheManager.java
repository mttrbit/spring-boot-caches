/*
 * Copyright 2002-2021 the original author or authors.
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

package spring.caches.backend.simple;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link CacheManager} implementation that lazily builds
 * {@link org.springframework.cache.concurrent.ConcurrentMapCache} instances for each
 * {@link #getCache} request. Also supports a 'static' mode where
 * the set of cache names is pre-defined through {@link #setCacheNames}, with no
 * dynamic creation of further cache regions at runtime.
 *
 * <p>Note: This is by no means a sophisticated CacheManager; it comes with no
 * cache configuration options. However, it may be useful for testing or simple
 * caching scenarios. For advanced local caching needs, consider
 * {@code spring.caches.backend.caffeine.CaffeineCacheManager}.
 *
 * @author Juergen Hoeller
 * @see org.springframework.cache.concurrent.ConcurrentMapCache
 * @since 3.1
 */
public class SimpleCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private final Map<String, Simple> namedCacheBuilders = new ConcurrentHashMap<>(16);

    private boolean dynamic = true;

    private boolean allowNullValues = true;

    /**
     * Construct a dynamic ConcurrentMapCacheManager,
     * lazily creating cache instances as they are being requested.
     */
    SimpleCacheManager() {
        // Intentionally left blank
    }

    SimpleCacheManager(Map<String, Simple> namedCacheBuilders) {
        this.namedCacheBuilders.putAll(namedCacheBuilders);
        for (Map.Entry<String, Simple> e : namedCacheBuilders.entrySet()) {
            this.cacheMap.put(e.getKey(), new SimpleCache(e.getKey(), e.getValue().build()));
        }
        this.dynamic = false;
    }

    /**
     * Recreate the common caches with the current state of this manager.
     */
    private void refreshCommonCaches() {
        for (Map.Entry<String, Cache> entry : this.cacheMap.entrySet()) {
            entry.setValue(createSimpleCache(entry.getKey()));
        }
    }

    /**
     * Return whether this cache manager accepts and converts {@code null} values
     * for all of its caches.
     */
    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    /**
     * Specify whether to accept and convert {@code null} values for all caches
     * in this cache manager.
     * <p>Default is "true", despite ConcurrentHashMap itself not supporting {@code null}
     * values. An internal holder object will be used to store user-level {@code null}s.
     * <p>Note: A change of the null-value setting will reset all existing caches,
     * if any, to reconfigure them with the new null-value requirement.
     */
    public void setAllowNullValues(boolean allowNullValues) {
        if (allowNullValues != this.allowNullValues) {
            this.allowNullValues = allowNullValues;
            // Need to recreate all Cache instances with the new null-value configuration...
            refreshCommonCaches();
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheMap.keySet());
    }

    /**
     * Specify the set of cache names for this CacheManager's 'static' mode.
     * <p>The number of caches and their names will be fixed after a call to this method,
     * with no creation of further cache regions at runtime.
     * <p>Calling this with a {@code null} collection argument resets the
     * mode to 'dynamic', allowing for further creation of caches again.
     */
    public final void setCacheNames(@Nullable Collection<String> cacheNames) {
        if (cacheNames != null) {
            for (String name : cacheNames) {
                this.cacheMap.put(name, createSimpleCache(name));
            }
            this.dynamic = false;
        } else {
            this.dynamic = true;
        }
    }

    @Override
    @Nullable
    public Cache getCache(String name) {
        return dynamic ? this.cacheMap.computeIfAbsent(name, this::createSimpleCache) : this.cacheMap.get(name);
    }

    final Cache createSimpleCache(String name) {
        return new SimpleCache(name, new Simple().allowNullValues(isAllowNullValues()).build(), isAllowNullValues());
    }

}

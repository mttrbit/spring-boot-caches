/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spring.caches.backend.elasticache.engines.memcached.stats;

import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.cache.Cache;

/**
 * Accumulates statistics during the operation of a {@link Cache} for presentation by
 * {@link spring.caches.backend.elasticache.engines.memcached.MemcachedCache#stats}. This is solely intended for consumption by {@code Cache} implementors.
 * <p>
 * Modified for the purpose of this project.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public interface StatsCounter {

    /**
     * Returns an accumulator that does not record any cache events.
     *
     * @return an accumulator that does not record metrics
     */
    static StatsCounter disabledStatsCounter() {
        return DisabledStatsCounter.INSTANCE;
    }

    /**
     * Returns an accumulator that suppresses and logs any exception thrown by the delegate
     * {@code statsCounter}.
     *
     * @param statsCounter the accumulator to delegate to
     * @return an accumulator that suppresses and logs any exception thrown by the delegate
     */
    static StatsCounter guardedStatsCounter(StatsCounter statsCounter) {
        return (statsCounter instanceof GuardedStatsCounter)
                ? statsCounter
                : new GuardedStatsCounter(statsCounter);
    }

    /**
     * Records cache hits. This should be called when a cache request returns a cached value.
     *
     * @param count the number of hits to record
     */
    void recordHits(@NonNegative int count);

    /**
     * Records cache misses. This should be called when a cache request returns a value that was not
     * found in the cache.
     *
     * @param count the number of misses to record
     */
    void recordMisses(@NonNegative int count);

    void recordLoads(@NonNegative int count);

    /**
     * Records the eviction of an entry from the cache.
     */
    void recordEviction(int count);

    /**
     * Returns a snapshot of this counter's values. Note that this may be an inconsistent view, as it
     * may be interleaved with update operations.
     *
     * @return a snapshot of this counter's values
     */
    CacheStats snapshot();
}

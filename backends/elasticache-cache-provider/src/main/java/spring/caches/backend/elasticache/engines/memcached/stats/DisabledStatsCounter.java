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

/**
 * A {@link StatsCounter} implementation that does not record any cache events.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
enum DisabledStatsCounter implements StatsCounter {
    INSTANCE;

    @Override
    public void recordHits(int count) {
        // Intentionally left blank.
    }

    @Override
    public void recordMisses(int count) {
        // Intentionally left blank.
    }

    @Override
    public void recordLoads(int count) {
        // Intentionally left blank.
    }

    @Override
    public void recordEviction(int count) {
        // Intentionally left blank.
    }

    @Override
    public CacheStats snapshot() {
        return CacheStats.empty();
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }
}

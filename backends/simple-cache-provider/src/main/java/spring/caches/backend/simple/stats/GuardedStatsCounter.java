/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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

package spring.caches.backend.simple.stats;

import org.checkerframework.checker.index.qual.NonNegative;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.util.Objects.requireNonNull;

/**
 * A {@link StatsCounter} implementation that suppresses and logs any exception thrown by the
 * delegate <tt>statsCounter</tt>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class GuardedStatsCounter implements StatsCounter {
    private static final Logger LOGGER = System.getLogger(GuardedStatsCounter.class.getName());

    private static final String MSG_WARN = "Exception thrown by stats counter";

    private final StatsCounter delegate;

    GuardedStatsCounter(StatsCounter delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void recordHits(int count) {
        try {
            delegate.recordHits(count);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, MSG_WARN, t);
        }
    }

    @Override
    public void recordMisses(int count) {
        try {
            delegate.recordMisses(count);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, MSG_WARN, t);
        }
    }

    @Override
    public void recordLoads(@NonNegative int count) {
        try {
            delegate.recordLoads(count);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, MSG_WARN, t);
        }
    }

    @Override
    public void recordEviction(int count) {
        try {
            delegate.recordEviction(count);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, MSG_WARN, t);
        }
    }

    @Override
    public CacheStats snapshot() {
        try {
            return delegate.snapshot();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, MSG_WARN, t);
            return CacheStats.empty();
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}

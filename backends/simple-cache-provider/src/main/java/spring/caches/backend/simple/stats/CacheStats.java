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

package spring.caches.backend.simple.stats;

import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import spring.caches.backend.simple.Cache;

import java.util.Objects;

/**
 * Statistics about the performance of a {@link Cache}.
 */
@Immutable
public final class CacheStats {
    private static final CacheStats EMPTY_STATS = CacheStats.of(0L, 0L, 0L, 0L);

    private final long hitCount;
    private final long missCount;
    private final long loadCount;
    private final long evictionCount;

    private CacheStats(
            @NonNegative long hitCount,
            @NonNegative long missCount,
            @NonNegative long loadCount,
            @NonNegative long evictionCount
    ) {
        if ((hitCount < 0) || (missCount < 0) || (evictionCount < 0) || (loadCount < 0)) {
            throw new IllegalArgumentException();
        }
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.loadCount = loadCount;
        this.evictionCount = evictionCount;
    }

    /**
     * Returns a {@code CacheStats} representing the specified statistics.
     *
     * @param hitCount      the number of cache hits
     * @param missCount     the number of cache misses
     * @param evictionCount the number of entries evicted from the cache
     * @return a {@code CacheStats} representing the specified statistics
     */
    public static CacheStats of(
            @NonNegative long hitCount,
            @NonNegative long missCount,
            @NonNegative long loadCount,
            @NonNegative long evictionCount
    ) {
        // Many parameters of the same type in a row is a bad thing, but this class is not constructed
        // by end users and is too fine-grained for a builder.
        return new CacheStats(hitCount, missCount, loadCount, evictionCount);
    }

    /**
     * Returns a statistics instance where no cache events have been recorded.
     *
     * @return an empty statistics instance
     */
    public static CacheStats empty() {
        return EMPTY_STATS;
    }

    /**
     * Returns the difference of {@code a} and {@code b} unless it would overflow or underflow in
     * which case {@code Long.MAX_VALUE} or {@code Long.MIN_VALUE} is returned, respectively.
     */
    @SuppressWarnings("ShortCircuitBoolean")
    private static long saturatedSubtract(long a, long b) {
        long naiveDifference = a - b;
        if (((a ^ b) >= 0) | ((a ^ naiveDifference) >= 0)) {
            // If a and b have the same signs or a has the same sign as the result then there was no
            // overflow, return.
            return naiveDifference;
        }
        // we did over/under flow
        return Long.MAX_VALUE + ((naiveDifference >>> (Long.SIZE - 1)) ^ 1);
    }

    /**
     * Returns the sum of {@code a} and {@code b} unless it would overflow or underflow in which case
     * {@code Long.MAX_VALUE} or {@code Long.MIN_VALUE} is returned, respectively.
     */
    @SuppressWarnings("ShortCircuitBoolean")
    private static long saturatedAdd(long a, long b) {
        long naiveSum = a + b;
        if (((a ^ b) < 0) | ((a ^ naiveSum) >= 0)) {
            // If a and b have different signs or a has the same sign as the result then there was no
            // overflow, return.
            return naiveSum;
        }
        // we did over/under flow, if the sign is negative we should return MAX otherwise MIN
        return Long.MAX_VALUE + ((naiveSum >>> (Long.SIZE - 1)) ^ 1);
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have returned either a cached or
     * uncached value. This is defined as {@code hitCount + missCount}.
     * <p>
     * <b>Note:</b> the values of the metrics are undefined in case of overflow (though it is
     * guaranteed not to throw an exception). If you require specific handling, we recommend
     * implementing your own stats collector.
     *
     * @return the {@code hitCount + missCount}
     */
    public @NonNegative long requestCount() {
        return saturatedAdd(hitCount, missCount);
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have returned a cached value.
     *
     * @return the number of times {@link Cache} lookup methods have returned a cached value
     */
    public @NonNegative long hitCount() {
        return hitCount;
    }

    /**
     * Returns the ratio of cache requests which were hits. This is defined as
     * {@code hitCount / requestCount}, or {@code 1.0} when {@code requestCount == 0}. Note that
     * {@code hitRate + missRate =~ 1.0}.
     *
     * @return the ratio of cache requests which were hits
     */
    public @NonNegative double hitRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have returned an uncached (newly
     * loaded) value, or null. Multiple concurrent calls to {@link Cache} lookup methods on an absent
     * value can result in multiple misses, all returning the results of a single cache load
     * operation.
     *
     * @return the number of times {@link Cache} lookup methods have returned an uncached (newly
     * loaded) value, or null
     */
    public @NonNegative long missCount() {
        return missCount;
    }

    public @NonNegative long loadCount() {
        return loadCount;
    }

    /**
     * Returns the ratio of cache requests which were misses. This is defined as
     * {@code missCount / requestCount}, or {@code 0.0} when {@code requestCount == 0}.
     * Note that {@code hitRate + missRate =~ 1.0}. Cache misses include all requests which
     * weren't cache hits, including requests which resulted in either successful or failed loading
     * attempts, and requests which waited for other threads to finish loading. It is thus the case
     * that {@code missCount >= loadSuccessCount + loadFailureCount}. Multiple
     * concurrent misses for the same key will result in a single load operation.
     *
     * @return the ratio of cache requests which were misses
     */
    public @NonNegative double missRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    /**
     * Returns the number of times an entry has been evicted. This count does not include manual
     * {@linkplain Cache#invalidate invalidations}.
     *
     * @return the number of times an entry has been evicted
     */
    public @NonNegative long evictionCount() {
        return evictionCount;
    }

    /**
     * Returns a new {@code CacheStats} representing the difference between this {@code CacheStats}
     * and {@code other}. Negative values, which aren't supported by {@code CacheStats} will be
     * rounded up to zero.
     *
     * @param other the statistics to subtract with
     * @return the difference between this instance and {@code other}
     */
    public CacheStats minus(CacheStats other) {
        return CacheStats.of(
                Math.max(0L, saturatedSubtract(hitCount, other.hitCount)),
                Math.max(0L, saturatedSubtract(missCount, other.missCount)),
                Math.max(0L, saturatedSubtract(loadCount, other.loadCount)),
                Math.max(0L, saturatedSubtract(evictionCount, other.evictionCount)));
    }

    /**
     * Returns a new {@code CacheStats} representing the sum of this {@code CacheStats} and
     * {@code other}.
     *
     * @param other the statistics to add with
     * @return the sum of the statistics
     */
    public CacheStats plus(CacheStats other) {
        return CacheStats.of(
                saturatedAdd(hitCount, other.hitCount),
                saturatedAdd(missCount, other.missCount),
                saturatedAdd(loadCount, other.loadCount),
                saturatedAdd(evictionCount, other.evictionCount));
    }

    @Override
    public int hashCode() {
        return Objects.hash(hitCount, missCount, loadCount, evictionCount);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CacheStats)) {
            return false;
        }
        CacheStats other = (CacheStats) o;
        return hitCount == other.hitCount
                && missCount == other.missCount
                && loadCount == other.loadCount
                && evictionCount == other.evictionCount;
    }

    @SuppressWarnings("MultipleStringLiterals")
    @Override
    public String toString() {
        return getClass().getSimpleName() + '{'
                + "hitCount=" + hitCount + ", "
                + "missCount=" + missCount + ", "
                + "loadCount=" + loadCount + ", "
                + "evictionCount=" + evictionCount + ", "
                + '}';
    }
}

/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.runtime.health.core.caching;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

/**
 * HealthIndicator wrapper implementation that caches the response
 *
 * @author elandau
 *
 */
public class CachingHealthIndicator implements HealthIndicator {
    private static class CacheEntry {
        private final long expirationTime;
        private final Health health;

        CacheEntry(long expirationTime, Health health) {
            super();
            this.expirationTime = expirationTime;
            this.health = health;
        }

        long getExpirationTime() {
            return expirationTime;
        }

        Health getHealth() {
            return health;
        }
    }

    private final AtomicBoolean lock;
    private final long interval;
    private final HealthIndicator delegate;
    private volatile CacheEntry cachedHealth;

    private CachingHealthIndicator(HealthIndicator delegate, long interval, TimeUnit units) {
        this.delegate = delegate;
        this.interval = TimeUnit.NANOSECONDS.convert(interval, units);
        this.lock = new AtomicBoolean(false);
        this.cachedHealth = new CacheEntry(0L, null);
    }

    @Override
    public void check(HealthIndicatorCallback callback) {
        CacheEntry cacheEntry = this.cachedHealth;
        long currentTime = System.nanoTime();
        if (currentTime > cacheEntry.getExpirationTime()) {
            if (lock.compareAndSet(false, true)) {
                try {
                    delegate.check(h -> {
                        this.cachedHealth = new CacheEntry(currentTime + interval,
                                Health.from(h).withDetail(Health.CACHE_KEY, true).build());
                        callback.inform(h);
                    });
                    return;
                } finally {
                    lock.set(false);
                }
            }
        }
        callback.inform(cacheEntry.getHealth());
    }

    public static CachingHealthIndicator wrap(HealthIndicator delegate, long interval, TimeUnit units) {
        return new CachingHealthIndicator(delegate, interval, units);
    }

    public String getName() {
        return delegate.getName();
    }
}

package com.netflix.runtime.health.api;

import com.netflix.runtime.health.api.internal.CachingHealthIndicator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class HealthIndicators {
    public static HealthIndicator alwaysHealthy(String name) {
        return memoize(name, HealthIndicatorStatuses.healthy(name));
    }

    public static HealthIndicator alwaysUnhealthy(String name) {
        return memoize(name, HealthIndicatorStatuses.unhealthy(name, new Exception("Unhealthy")));
    }

    public static HealthIndicator memoize(final String name, final HealthIndicatorStatus status) {
        return new HealthIndicator() {
            @Override
            public CompletableFuture<HealthIndicatorStatus> check() {
                return CompletableFuture.completedFuture(status);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public static HealthIndicator cache(HealthIndicator delegate, long interval, TimeUnit units) {
        return CachingHealthIndicator.cache(delegate, interval, units);
    }
}

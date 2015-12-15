package com.netflix.runtime.health.core;

import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class SimpleHealthCheck implements HealthCheck {
    private final HealthIndicatorRegistry registry;

    public SimpleHealthCheck(HealthIndicatorRegistry registry) {
        this.registry = registry;
    }

    public String getName() {
        return "root";
    }

    public CompletableFuture<HealthCheckStatus> check() {
        final CompletableFuture<List<HealthIndicatorStatus>> future = new CompletableFuture<>();

        final List<HealthIndicator> indicators = registry.getHealthIndicators();
        final List<HealthIndicatorStatus> statuses = new CopyOnWriteArrayList<>();
        if (indicators.isEmpty()) {
            future.complete(statuses);
        } else {
            // Run all the HealthIndicators and collect the statuses.
            final AtomicInteger counter = new AtomicInteger(indicators.size());
            for (HealthIndicator indicator : indicators) {
                indicator.check().thenAccept((result) -> {
                    // Aggregate the health checks
                    statuses.add(result);

                    // Reached the last health check so complete the future
                    if (counter.decrementAndGet() == 0) {
                        future.complete(statuses);
                    }
                });
            }
        }

        return future.thenApply((t) -> {
            HealthCheckStatus.HealthState state = calcIsHealthy(t)
                    ? HealthCheckStatus.HealthState.Healthy
                    : HealthCheckStatus.HealthState.Unhealthy;
            return new HealthCheckStatus(state, t);
        });
    }

    /**
     * Return false is any of the health indicators are unhealthy.
     */
    private boolean calcIsHealthy(List<HealthIndicatorStatus> statuses) {
        for (HealthIndicatorStatus status : statuses) {
            if (!status.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}
package com.netflix.runtime.health.api;

import java.util.concurrent.CompletableFuture;

/**
 */
public interface HealthCheckAggregator {
    CompletableFuture<HealthCheckStatus> check();
}

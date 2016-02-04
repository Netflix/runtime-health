package com.netflix.runtime.health.core;

import java.util.concurrent.CompletableFuture;

/**
 */
public interface HealthCheckAggregator {
    CompletableFuture<HealthCheckStatus> check();
}

package com.netflix.runtime.health.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 */
public interface HealthCheckAggregator {
    CompletableFuture<HealthCheckStatus> check();
    
    CompletableFuture<HealthCheckStatus> check(long maxWait, TimeUnit units);
}

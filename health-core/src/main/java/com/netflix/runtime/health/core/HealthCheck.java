package com.netflix.runtime.health.core;

import java.util.concurrent.CompletableFuture;

/**
 */
public interface HealthCheck {

    String getName();

    CompletableFuture<HealthCheckStatus> check();
}

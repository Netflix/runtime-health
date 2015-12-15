package com.netflix.runtime.health.governator;

import com.netflix.governator.LifecycleListener;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;
import com.netflix.runtime.health.api.HealthIndicatorStatuses;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class InjectorHealthIndicator implements LifecycleListener, HealthIndicator {

    private volatile Throwable error;

    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return (error != null)
                ? CompletableFuture.completedFuture(HealthIndicatorStatuses.unhealthy(getName(), error))
                : CompletableFuture.completedFuture(HealthIndicatorStatuses.healthy(getName()));
    }

    @Override
    public String getName() {
        return "governator";
    }

    @Override
    public void onStarted() {
    }

    @Override
    public void onStopped() {
    }

    @Override
    public void onStartFailed(Throwable t) {
        error = t;
    }
}

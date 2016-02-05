package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public final class AsyncHealthCheckAggregator implements HealthCheckAggregator {

    private final List<HealthIndicator> indicators;
    private final ScheduledExecutorService executor;
    
    public AsyncHealthCheckAggregator(List<HealthIndicator> indicators) {
        this(indicators, Executors.newSingleThreadScheduledExecutor());
    }
    
    public AsyncHealthCheckAggregator(List<HealthIndicator> indicators, ScheduledExecutorService executor) {
        this.indicators = new ArrayList<>(indicators);
        this.executor = executor;
    }
    
    public CompletableFuture<HealthCheckStatus> check() {
        return check(0, null);
    }
    
    @Override
    public CompletableFuture<HealthCheckStatus> check(long maxWaitTime, TimeUnit units) {
        final List<HealthIndicatorCallbackImpl> callbacks = new ArrayList<>(indicators.size());
        final CompletableFuture<HealthCheckStatus> future = new CompletableFuture<HealthCheckStatus>();
        
        final AtomicInteger counter = new AtomicInteger(indicators.size());
        for (HealthIndicator indicator : indicators) {
            HealthIndicatorCallbackImpl callback = new HealthIndicatorCallbackImpl(indicator) {
                @Override
                public void inform(Health status) {
                    setHealth(status);
                    if (counter.decrementAndGet() == 0) {
                        future.complete(getStatusFromCallbacks(callbacks));
                    }
                }
            };

            callbacks.add(callback);
            indicator.check(callback);
        }
        
        if (maxWaitTime != 0 && units != null) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    future.complete(getStatusFromCallbacks(callbacks));
                }
            }, maxWaitTime, units);
        }
        return future;
    }

    HealthCheckStatus getStatusFromCallbacks(List<HealthIndicatorCallbackImpl> callbacks) {
        List<Health> healths = new ArrayList<>(callbacks.size());
        boolean isHealthy = true;
        for (HealthIndicatorCallbackImpl callback : callbacks) {
            Health health = callback.getHealthOrTimeout();
            if (!health.isHealthy()) {
                isHealthy = false;
            }
            healths.add(health);
        }
        
        return HealthCheckStatus.create(isHealthy, healths);
    }
    
    abstract class HealthIndicatorCallbackImpl implements HealthIndicatorCallback {
        private volatile Health health;

        private final HealthIndicator indicator;
        
        HealthIndicatorCallbackImpl(HealthIndicator indicator) {
            this.indicator = indicator;
        }
        
        void setHealth(Health health) {
            this.health = health;
        }
        
        public Health getHealthOrTimeout() {
            return health != null 
                ? health 
                : Health
                    .unhealthy(new TimeoutException("Timedout waiting for response"))
                    .withDetail("className", indicator.getClass().getName())
                    .build();
        }
    }
}
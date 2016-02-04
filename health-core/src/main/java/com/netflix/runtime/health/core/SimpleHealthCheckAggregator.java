package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

/**
 */
public class SimpleHealthCheckAggregator implements HealthCheckAggregator {
  
	protected final List<HealthIndicator> indicators;

    public SimpleHealthCheckAggregator(List<HealthIndicator> indicators) {
        this.indicators = new ArrayList<>(indicators);
    }

    public CompletableFuture<HealthCheckStatus> check() {
        final CompletableFuture<List<Health>> future = new CompletableFuture<>(); 
        final List<Health> statuses = indicators.stream()
        			.map(this::getStatusUsingCallback)
        			.collect(Collectors.toList());
        future.complete(statuses);
        return future.thenApply(t -> new HealthCheckStatus(determineIfHealthy(t), t));
    }

    protected Health getStatusUsingCallback(HealthIndicator indicator) {
    	SimpleHealthIndicatorCallback simpleHealthIndicatorCallback = new SimpleHealthIndicatorCallback();
    	indicator.check(simpleHealthIndicatorCallback);
    	return simpleHealthIndicatorCallback.getHealth(); 	
    }
    
    /**
     * Return false is any of the health indicators are unhealthy.
     */
    protected boolean determineIfHealthy(List<Health> statuses) {
    	return statuses.stream()
    					.map(indicator -> indicator.isHealthy())
    					.reduce(true, (a,b)-> a && b);
    }
    
    protected class SimpleHealthIndicatorCallback implements HealthIndicatorCallback {

    	private Health health;

    	@Override
    	public void complete(Health health) {
    		this.health = health;
    	}

    	public Health getHealth() {
    		return health;
    	}
    }
}
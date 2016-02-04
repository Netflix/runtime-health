package com.netflix.runtime.health.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.runtime.health.api.HealthIndicatorStatus;

/**
 */
public class SimpleHealthCheckAggregator implements HealthCheckAggregator {
  
	protected List<HealthIndicator> indicators;

    public SimpleHealthCheckAggregator(List<HealthIndicator> healthIndicator) {
        this.indicators = healthIndicator;
    }

    public CompletableFuture<HealthCheckStatus> check() {
        final CompletableFuture<List<HealthIndicatorStatus>> future = new CompletableFuture<>(); 
        final List<HealthIndicatorStatus> statuses = indicators.stream()
        			.map(this::getStatusUsingCallback)
        			.collect(Collectors.toList());
        future.complete(statuses);
        return future.thenApply(t -> new HealthCheckStatus(determineIfHealthy(t), t));
    }

    protected HealthIndicatorStatus getStatusUsingCallback(HealthIndicator indicator) {
    	SimpleHealthIndicatorCallback simpleHealthIndicatorCallback = new SimpleHealthIndicatorCallback();
    	indicator.check(simpleHealthIndicatorCallback);
    	return simpleHealthIndicatorCallback.getHealthIndicatorStatus(); 	
    }
    
    /**
     * Return false is any of the health indicators are unhealthy.
     */
    protected boolean determineIfHealthy(List<HealthIndicatorStatus> statuses) {
    	return statuses.stream()
    					.map(indicator -> indicator.isHealthy())
    					.reduce(true, (a,b)-> a && b);
    }
    
    protected class SimpleHealthIndicatorCallback implements HealthIndicatorCallback {

    	private HealthIndicatorStatus healthIndicatorStatus;

    	@Override
    	public void complete(HealthIndicatorStatus status) {
    		this.healthIndicatorStatus = status;
    	}

    	public HealthIndicatorStatus getHealthIndicatorStatus() {
    		return healthIndicatorStatus;
    	}
    }
}
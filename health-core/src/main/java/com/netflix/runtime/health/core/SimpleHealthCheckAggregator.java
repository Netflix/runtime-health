package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

/**
 */
public class SimpleHealthCheckAggregator implements HealthCheckAggregator {
  
	private final List<HealthIndicator> indicators;
	private final ScheduledExecutorService executor;
	private final TimeUnit units;
	private final long maxWaitTime;

    public SimpleHealthCheckAggregator(List<HealthIndicator> indicators, long maxWaitTime, TimeUnit units) {
        this.indicators = new ArrayList<>(indicators);
        this.maxWaitTime = maxWaitTime;
        this.units = units;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public CompletableFuture<HealthCheckStatus> check() {
        final List<HealthIndicatorCallbackImpl> callbacks = new ArrayList<>(indicators.size());
        final CompletableFuture<HealthCheckStatus> future = new CompletableFuture<HealthCheckStatus>();
        final AtomicInteger counter = new AtomicInteger(indicators.size());
        
        List<CompletableFuture<?>> futures = indicators.stream().map(indicator -> {

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
      
            return CompletableFuture.runAsync(()-> {
	            try {
	            	indicator.check(callback);
	            }
	            catch(Exception ex) 
	            {
	            	callback.inform(Health.unhealthy(ex).build());
	            }
            });
            
        }).collect(Collectors.toList());
        
        
        
        if(indicators.size() == 0) {
        	future.complete(HealthCheckStatus.create(true, Collections.emptyList()));
        }
        
        if (maxWaitTime != 0 && units != null) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    future.complete(getStatusFromCallbacks(callbacks));
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).cancel(true);
                }
            }, maxWaitTime, units);
        }
    
        return future;
    }

	protected HealthCheckStatus getStatusFromCallbacks(List<HealthIndicatorCallbackImpl> callbacks) {
	    List<Health> healths = new ArrayList<>(callbacks.size());
	    boolean isHealthy = callbacks.stream()
		    .map(callback -> {
		    	Health health = Health.from(callback.getHealthOrTimeout())
		    			.withDetail("className", callback.getIndicator().getClass().getName())
		    			.build();
		    	healths.add(health);
		    	return health;
		    })
		    .map(health -> health.isHealthy())
		    .reduce(true, (a,b) -> a && b); 
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
                    .unhealthy(new TimeoutException("Timed out waiting for response"))
                    .build();
        }
        
        public HealthIndicator getIndicator() {
        	return this.indicator;
        }
    }
}
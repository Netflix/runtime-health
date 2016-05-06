/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.runtime.health.api.IndicatorMatcher;
import com.netflix.runtime.health.api.IndicatorMatchers;

/**
 */
public class SimpleHealthCheckAggregator implements HealthCheckAggregator {
  
	private final List<HealthIndicator> indicators;
	private final ScheduledExecutorService executor;
	private final TimeUnit units;
	private final long maxWaitTime;
	private final ApplicationEventDispatcher eventDispatcher;
	private final AtomicBoolean previousHealth;
	
    public SimpleHealthCheckAggregator(List<HealthIndicator> indicators, long maxWaitTime, TimeUnit units) {
	    this(indicators, maxWaitTime, units, null);
	}

    public SimpleHealthCheckAggregator(List<HealthIndicator> indicators, long maxWaitTime, TimeUnit units,
            ApplicationEventDispatcher eventDispatcher) {
        this.indicators = new ArrayList<>(indicators);
        this.maxWaitTime = maxWaitTime;
        this.units = units;
        this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {  
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "healthIndicatorMonitor");
                thread.setDaemon(true);
                return thread;
            }
        });
        this.eventDispatcher = eventDispatcher;
        this.previousHealth = new AtomicBoolean();
    }
    
    @Override
    public CompletableFuture<HealthCheckStatus> check() {
        return check(IndicatorMatchers.build());
    }

    public CompletableFuture<HealthCheckStatus> check(IndicatorMatcher matcher) {
        final List<HealthIndicatorCallbackImpl> callbacks = new ArrayList<>(indicators.size());
        final CompletableFuture<HealthCheckStatus> future = new CompletableFuture<HealthCheckStatus>();
        final AtomicInteger counter = new AtomicInteger(indicators.size());
        
        if (eventDispatcher != null) {
            future.whenComplete((h, e) -> {
                if (h != null && previousHealth.compareAndSet(!h.isHealthy(), h.isHealthy())) {
                    eventDispatcher.publishEvent(new HealthCheckStatusChangedEvent(h));
                }
            });
        }
        
        List<CompletableFuture<?>> futures = indicators.stream().map(indicator -> {

            HealthIndicatorCallbackImpl callback = new HealthIndicatorCallbackImpl(indicator, !matcher.matches(indicator)) {
                @Override
                public void inform(Health status) {
                    setHealth(status);
                    if (counter.decrementAndGet() == 0) {
                        future.complete(getStatusFromCallbacks(callbacks));
                    }
                }
            };

            callbacks.add(callback);
      
            return CompletableFuture.runAsync(() -> {
                try {
                    indicator.check(callback);
                } catch (Exception ex) {
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

	protected HealthCheckStatus getStatusFromCallbacks(final List<HealthIndicatorCallbackImpl> callbacks) {
	    List<Health> healths = new ArrayList<>();
	    List<Health> suppressedHealths = new ArrayList<>();  
	    boolean isHealthy = callbacks.stream()
		    .map(callback -> {
		    	Health health = Health.from(callback.getHealthOrTimeout())
		    			.withDetail(Health.NAME_KEY, callback.getIndicator().getName()).build();
		    	if(callback.isSuppressed()) {
		    	    suppressedHealths.add(health);
		    	    return Health.healthy().build();
		    	} else {
		    	    healths.add(health);
		    	    return health;
		    	}
		    })
		    .map(health -> health.isHealthy())
		    .reduce(true, (a,b) -> a && b); 
	    return HealthCheckStatus.create(isHealthy, healths, suppressedHealths);
	}
	     
    abstract class HealthIndicatorCallbackImpl implements HealthIndicatorCallback {
        private volatile Health health;
        private final HealthIndicator indicator;
        private final boolean suppressed;
        
        HealthIndicatorCallbackImpl(HealthIndicator indicator, boolean suppressed) {
            this.indicator = indicator;
            this.suppressed = suppressed;
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
        
        public boolean isSuppressed() {
            return this.suppressed;
        }
    }
}
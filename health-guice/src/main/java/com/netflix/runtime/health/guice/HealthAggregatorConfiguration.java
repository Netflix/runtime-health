package com.netflix.runtime.health.guice;

import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthIndicator;

@Configuration(prefix="health.aggregator")
public interface HealthAggregatorConfiguration {

    /***
     * Should health indicator results be cached between invocations.
     */
    @DefaultValue("true")
    boolean cacheHealthIndicators();
    
    /***
     * Number of milliseconds for which the {@link HealthCheckAggregator} should cache the response of
     * any {@link HealthIndicator}.
     */
    @DefaultValue("5000")
    long getCacheIntervalInMillis();
    
    /***
     * Number of milliseconds for which the {@link HealthCheckAggregator} should wait for the response
     * of any {@link HealthIndicator} before considering it as unhealthy and canceling invocation.
     */
    @DefaultValue("1000")
    long getAggregatorWaitIntervalInMillis();
    
}

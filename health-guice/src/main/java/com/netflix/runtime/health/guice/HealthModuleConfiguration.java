package com.netflix.runtime.health.guice;

import java.util.concurrent.TimeUnit;

import com.netflix.archaius.api.annotations.DefaultValue;

public interface HealthModuleConfiguration {

    @DefaultValue("true")
    boolean getCacheHealthIndicators();
    
    @DefaultValue("5")
    long getCacheInterval();
    
    @DefaultValue("SECONDS")
    TimeUnit getCacheIntervalUnits();
    
    @DefaultValue("1")
    long getAggregatorWaitInterval();
    
    @DefaultValue("SECONDS")
    TimeUnit getAggregatorWaitIntervalUnits();
    

}

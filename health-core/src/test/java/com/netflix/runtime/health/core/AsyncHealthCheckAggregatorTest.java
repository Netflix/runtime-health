package com.netflix.runtime.health.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public class AsyncHealthCheckAggregatorTest {
    static HealthIndicator nonResponsive = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
        }
    };
    
    static HealthIndicator unhealthy = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.unhealthy().build());
        }
    };
    
    static HealthIndicator healthy = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    };
    
    @Test
    public void emptyIndicatorListReturnsHealthy() throws Exception {
        List<HealthIndicator> indicators = Collections.emptyList(); 
        AsyncHealthCheckAggregator check = new AsyncHealthCheckAggregator(indicators);
        
        HealthCheckStatus result = check.check(1, TimeUnit.SECONDS).get();
        Assert.assertTrue(result.isHealthy());
        Assert.assertEquals(0, result.getIndicators().size());
    }
    
    @Test
    public void immediateHealthy() throws Exception {
        List<HealthIndicator> indicators = Arrays.<HealthIndicator>asList(healthy); 
        AsyncHealthCheckAggregator check = new AsyncHealthCheckAggregator(indicators);
        
        HealthCheckStatus result = check.check(1, TimeUnit.SECONDS).get();
        Assert.assertTrue(result.isHealthy());
        Assert.assertEquals(1, result.getIndicators().size());
    }
    
    @Test
    public void immediateUnhealth() throws Exception {
        List<HealthIndicator> indicators = Arrays.<HealthIndicator>asList(unhealthy); 
        AsyncHealthCheckAggregator check = new AsyncHealthCheckAggregator(indicators);
        
        HealthCheckStatus result = check.check(1, TimeUnit.SECONDS).get();
        Assert.assertFalse(result.isHealthy());
        Assert.assertEquals(1, result.getIndicators().size());
    }
    
    @Test
    public void nonResponsiveIndicatorReturnsUnhealthy() throws Exception {
        List<HealthIndicator> indicators = Arrays.<HealthIndicator>asList(healthy, nonResponsive); 
        AsyncHealthCheckAggregator check = new AsyncHealthCheckAggregator(indicators);
        
        HealthCheckStatus result = check.check(1, TimeUnit.SECONDS).get();
        Assert.assertFalse(result.isHealthy());
        Assert.assertEquals(2, result.getIndicators().size());
        Assert.assertEquals(1, result.getIndicators().stream().filter((t) -> t.isHealthy()).count());
    }
    
    @Test
    public void timeoutAndApply() throws Exception {
        List<HealthIndicator> indicators = Arrays.<HealthIndicator>asList(nonResponsive); 
        AsyncHealthCheckAggregator check = new AsyncHealthCheckAggregator(indicators);
        
        boolean result = check.check(1, TimeUnit.MILLISECONDS)
                .thenApply((t) -> t.isHealthy())
                .get(1, TimeUnit.SECONDS);
    }
}

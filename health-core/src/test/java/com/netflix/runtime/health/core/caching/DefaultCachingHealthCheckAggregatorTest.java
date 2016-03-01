package com.netflix.runtime.health.core.caching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public class DefaultCachingHealthCheckAggregatorTest {

    @Test(timeout=1000)
    public void testCachedIndicatorNameNotHidden() throws Exception {
        DefaultCachingHealthCheckAggregator aggregator = new DefaultCachingHealthCheckAggregator(
                Arrays.asList(new TestHealthIndicator()), 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS, null);
        HealthCheckStatus aggregatedHealth = aggregator.check().get();
        assertTrue(aggregatedHealth.isHealthy());
        assertEquals(1, aggregatedHealth.getHealthResults().size());
        assertEquals(TestHealthIndicator.class.getName(), aggregatedHealth.getHealthResults().get(0).getDetails().get("className"));
    }
    
    private static class TestHealthIndicator implements HealthIndicator { 
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    }
}

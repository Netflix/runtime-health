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

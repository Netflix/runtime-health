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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.spectator.api.DefaultRegistry;

@RunWith(MockitoJUnitRunner.class)
public class SimpleHealthCheckAggregatorMetricsTest {

    @Mock ApplicationEventDispatcher dispatcher;
    DefaultRegistry registry;
    SimpleHealthCheckAggregator aggregator;
    
    @Before
    public void init() {
        this.registry = new DefaultRegistry();
    }
    
    static HealthIndicator nonResponsive = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
        }
    };
        
    static HealthIndicator exceptional = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            throw new RuntimeException("Boom");
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
    
    @Test(timeout=1000)
    public void testHealthyIsRecorded() throws Exception {
        aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy), 1, TimeUnit.SECONDS, dispatcher, registry);
        aggregator.check().get();
        assertEquals(1, registry.counter("runtime.health", "status", "HEALTHY").count());
    }
    
    @Test(timeout=1000)
    public void testUnHealthyIsRecorded() throws Exception {
        aggregator = new SimpleHealthCheckAggregator(Arrays.asList(unhealthy), 1, TimeUnit.SECONDS, dispatcher, registry);
        aggregator.check().get();
        assertEquals(1, registry.counter("runtime.health", "status", "UNHEALTHY").count());
    }
    
    @Test(timeout=1000)
    public void testTimeoutIsRecorded() throws Exception {
        aggregator = new SimpleHealthCheckAggregator(Arrays.asList(nonResponsive), 50, TimeUnit.MILLISECONDS, dispatcher, registry);
        aggregator.check().get();
        assertEquals(1, registry.counter("runtime.health", "status", "TIMEOUT").count());
    }
}

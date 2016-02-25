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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public class SimpleHealthCheckAggregatorTest {
	
	SimpleHealthCheckAggregator aggregator;
	
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
	public void testEmptyListIsHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(new ArrayList<>(), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertTrue(aggregatedHealth.isHealthy());
		assertEquals(0, aggregatedHealth.getHealthResults().size());
	}
	
	@Test(timeout=1000)
	public void testAllHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,healthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertTrue(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
	}
	
	@Test(timeout=1000)
	public void testOneUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,unhealthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
	}
	
	@Test(timeout=1000)
	public void testAllUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(unhealthy,unhealthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
	}
	
	@Test(timeout=1000)
	public void testOneHealthyAndOneExceptional() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,exceptional), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
	}
	
	@Test(timeout=1000)
	public void testOneHealthyAndOneNonResponsive() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,nonResponsive), 50, TimeUnit.MILLISECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
		Health failed = aggregatedHealth.getHealthResults().stream().filter(h->!h.isHealthy()).findFirst().get();
		assertNotNull(failed);
		assertTrue(failed.getErrorMessage().isPresent());
		assertEquals("java.util.concurrent.TimeoutException: Timed out waiting for response", failed.getErrorMessage().get());
	}

	
	@Test(timeout=1000)
	public void testWithDetails() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				(callback)->callback.inform(Health.healthy().withDetail("foo", "bar").build()),
				(callback)->callback.inform(Health.unhealthy(new RuntimeException("Boom")).build())), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getHealthResults().size());
		List<Health> indicators = aggregator.check().get().getHealthResults();
		assertThat(indicators).flatExtracting(s->s.getDetails().keySet()).contains("foo", "error");
		assertThat(indicators).flatExtracting(s->s.getDetails().values()).contains("bar", "java.lang.RuntimeException: Boom");
	}
	
	@Test(timeout=1000)
	public void testClassNameAdded() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,unhealthy,nonResponsive), 10, TimeUnit.MILLISECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(3, aggregatedHealth.getHealthResults().size());
		assertThat(aggregatedHealth.getHealthResults()).extracting(h->h.getDetails().get("className")).isNotNull();
	}
	
}
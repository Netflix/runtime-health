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
	
	@Test(timeout=100)
	public void testEmptyListIsHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(new ArrayList<>(), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertTrue(aggregatedHealth.isHealthy());
		assertEquals(0, aggregatedHealth.getIndicators().size());
	
	}
	
	@Test(timeout=100)
	public void testAllHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,healthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertTrue(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
	}
	
	@Test(timeout=100)
	public void testOneUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,unhealthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
	}
	
	@Test(timeout=100)
	public void testAllUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(unhealthy,unhealthy), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
	}
	
	@Test(timeout=100)
	public void testOneHealthyAndOneExceptional() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,exceptional), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
	}
	
	@Test(timeout=100)
	public void testOneHealthyAndOneNonResponsive() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,nonResponsive), 50, TimeUnit.MILLISECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
		Health failed = aggregatedHealth.getIndicators().stream().filter(h->!h.isHealthy()).findFirst().get();
		assertNotNull(failed);
		assertTrue(failed.getErrorMessage().isPresent());
		assertEquals("java.util.concurrent.TimeoutException: Timed out waiting for response", failed.getErrorMessage().get());
	}

	
	@Test(timeout=100)
	public void testWithDetails() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				(callback)->callback.inform(Health.healthy().withDetail("foo", "bar").build()),
				(callback)->callback.inform(Health.unhealthy(new RuntimeException("Boom")).build())), 1, TimeUnit.SECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(2, aggregatedHealth.getIndicators().size());
		List<Health> indicators = aggregator.check().get().getIndicators();
		assertThat(indicators).flatExtracting(s->s.getDetails().keySet()).contains("foo", "error");
		assertThat(indicators).flatExtracting(s->s.getDetails().values()).contains("bar", "java.lang.RuntimeException: Boom");
	}
	
	@Test(timeout=100)
	public void testClassNameAdded() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList(healthy,unhealthy,nonResponsive), 10, TimeUnit.MILLISECONDS);
		HealthCheckStatus aggregatedHealth = aggregator.check().get();
		assertFalse(aggregatedHealth.isHealthy());
		assertEquals(3, aggregatedHealth.getIndicators().size());
		assertThat(aggregatedHealth.getIndicators()).extracting(h->h.getDetails().get("className")).isNotNull();
		
	}
	
}
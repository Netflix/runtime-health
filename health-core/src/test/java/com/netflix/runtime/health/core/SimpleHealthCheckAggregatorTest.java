package com.netflix.runtime.health.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.runtime.health.api.HealthIndicatorStatus;

public class SimpleHealthCheckAggregatorTest {
	
	SimpleHealthCheckAggregator aggregator;
	
	@Test
	public void testEmptyListIsHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(new ArrayList<>());
		assertTrue(aggregator.check().get().getState());	
	}
	
	@Test
	public void testAllHealthy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				new TestHealthIndicator(Health.healthy().build()),
				new TestHealthIndicator(Health.healthy().build())));
		assertTrue(aggregator.check().get().getState());	
	}
	
	@Test
	public void testOneUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				new TestHealthIndicator(Health.healthy().build()),
				new TestHealthIndicator(Health.unhealthy().build())));
		assertFalse(aggregator.check().get().getState());
	}
	
	@Test
	public void testAllUnheathy() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				new TestHealthIndicator(Health.unhealthy().build()),
				new TestHealthIndicator(Health.unhealthy().build())));
		assertFalse(aggregator.check().get().getState());
	}
	
	@Test
	public void testWithDetails() throws Exception {
		aggregator = new SimpleHealthCheckAggregator(Arrays.asList( 
				new TestHealthIndicator(Health.healthy().withDetail("foo", "bar").build()),
				new TestHealthIndicator(Health.unhealthy(new RuntimeException("Boom")).build())));
		assertFalse(aggregator.check().get().getState());
		List<HealthIndicatorStatus> indicators = aggregator.check().get().getIndicators();
		assertThat(indicators).flatExtracting(s->s.getDetails().keySet()).contains("foo", "error");
		assertThat(indicators).flatExtracting(s->s.getDetails().values()).contains("bar", "java.lang.RuntimeException: Boom");
	}
	
	private class TestHealthIndicator implements HealthIndicator {

		private HealthIndicatorStatus status;
		public TestHealthIndicator(HealthIndicatorStatus status) {
			this.status = status;
		}
		
		@Override
		public void check(HealthIndicatorCallback health) {
			health.complete(status);
		}
	}
}
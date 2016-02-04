package com.netflix.runtime.health.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class HealthBuilderTest {
	
	@Test
	public void healthyStatus() {
		HealthIndicatorStatus status = Health.healthy().build();
		assertNotNull(status);
		assertNotNull(status.isHealthy());
		assertTrue(status.isHealthy());
		assertNotNull(status.getDetails());
		assertTrue(status.getDetails().isEmpty());
	}
	
	@Test
	public void healthyStatusWithDetails() {
		HealthIndicatorStatus status = Health.healthy()
										.withDetail("foo", "bar")
										.withDetail("baz", "qux").build();
		assertNotNull(status);
		assertNotNull(status.isHealthy());
		assertTrue(status.isHealthy());
		assertNotNull(status.getDetails());
		assertFalse(status.getDetails().isEmpty());
		assertEquals("bar", status.getDetails().get("foo"));
		assertEquals("qux", status.getDetails().get("baz"));
		assertNull(status.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatus() {
		HealthIndicatorStatus status = Health.unhealthy().build();
		assertNotNull(status);
		assertNotNull(status.isHealthy());
		assertFalse(status.isHealthy());
		assertNotNull(status.getDetails());
		assertTrue(status.getDetails().isEmpty());
		assertNull(status.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatusWithExceptoin() {
		HealthIndicatorStatus status = Health.unhealthy()
										.withException(new RuntimeException("Boom"))
										.build();
		assertNotNull(status);
		assertNotNull(status.isHealthy());
		assertFalse(status.isHealthy());
		assertNotNull(status.getDetails());
		assertFalse(status.getDetails().isEmpty());
		assertEquals("java.lang.RuntimeException: Boom", status.getDetails().get("error"));
		assertEquals("java.lang.RuntimeException: Boom", status.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatusWithDetails() {
		HealthIndicatorStatus status = Health.unhealthy()
										.withException(new RuntimeException("Boom"))
										.withDetail("foo", "bar")
										.withDetail("baz", "qux").build();
		assertNotNull(status);
		assertNotNull(status.isHealthy());
		assertFalse(status.isHealthy());
		assertNotNull(status.getDetails());
		assertFalse(status.getDetails().isEmpty());
		assertEquals("bar", status.getDetails().get("foo"));
		assertEquals("qux", status.getDetails().get("baz"));
		assertEquals("java.lang.RuntimeException: Boom", status.getDetails().get("error"));
		assertEquals("java.lang.RuntimeException: Boom", status.getErrorMessage());
	}

}

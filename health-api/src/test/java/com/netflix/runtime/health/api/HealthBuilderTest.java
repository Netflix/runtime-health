package com.netflix.runtime.health.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HealthBuilderTest {
	
	@Test
	public void healthyStatus() {
		Health health = Health.healthy().build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertTrue(health.isHealthy());
		assertNotNull(health.getDetails());
		assertTrue(health.getDetails().isEmpty());
	}
	
	@Test
	public void healthyStatusWithDetails() {
		Health health = Health.healthy()
										.withDetail("foo", "bar")
										.withDetail("baz", "qux").build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertTrue(health.isHealthy());
		assertNotNull(health.getDetails());
		assertFalse(health.getDetails().isEmpty());
		assertEquals("bar", health.getDetails().get("foo"));
		assertEquals("qux", health.getDetails().get("baz"));
		assertNull(health.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatus() {
		Health health = Health.unhealthy().build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertFalse(health.isHealthy());
		assertNotNull(health.getDetails());
		assertTrue(health.getDetails().isEmpty());
		assertNull(health.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatusWithExceptoin() {
		Health health = Health.unhealthy()
										.withException(new RuntimeException("Boom"))
										.build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertFalse(health.isHealthy());
		assertNotNull(health.getDetails());
		assertFalse(health.getDetails().isEmpty());
		assertEquals("java.lang.RuntimeException: Boom", health.getDetails().get(Health.ERROR_KEY));
		assertEquals("java.lang.RuntimeException: Boom", health.getErrorMessage());
	}
	
	@Test
	public void unhealthyStatusWithDetails() {
		Health health = Health.unhealthy()
										.withException(new RuntimeException("Boom"))
										.withDetail("foo", "bar")
										.withDetail("baz", "qux").build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertFalse(health.isHealthy());
		assertNotNull(health.getDetails());
		assertFalse(health.getDetails().isEmpty());
		assertEquals("bar", health.getDetails().get("foo"));
		assertEquals("qux", health.getDetails().get("baz"));
		assertEquals("java.lang.RuntimeException: Boom", health.getDetails().get(Health.ERROR_KEY));
		assertEquals("java.lang.RuntimeException: Boom", health.getErrorMessage());
	}

}

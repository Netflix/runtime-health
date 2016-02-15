package com.netflix.runtime.health.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
		assertFalse(health.getErrorMessage().isPresent());
	}
	
	@Test
	public void unhealthyStatus() {
		Health health = Health.unhealthy().build();
		assertNotNull(health);
		assertNotNull(health.isHealthy());
		assertFalse(health.isHealthy());
		assertNotNull(health.getDetails());
		assertTrue(health.getDetails().isEmpty());
		assertFalse(health.getErrorMessage().isPresent());
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
		assertEquals("java.lang.RuntimeException: Boom", health.getErrorMessage().get());
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
		assertEquals("java.lang.RuntimeException: Boom", health.getErrorMessage().get());
	}
	
	@Test
	public void exceptionOnNullDetailKey() {
		assertThatThrownBy(() -> Health.healthy().withDetail(null, "value"))
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("Key must not be null");
	}
	
	@Test
	public void exceptionOnNullDetailValue() {
		assertThatThrownBy(() -> Health.healthy().withDetail("key", null))
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("Data must not be null");
	}
	
	@Test
	public void exceptionOnReservedErrorKeySet() {
		assertThatThrownBy(() -> Health.healthy().withDetail("error", "some error"))
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessageContaining("\"error\" is a reserved key and may not be overridden");
	}

}

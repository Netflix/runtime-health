package com.netflix.runtime.health.api;

/***
 * Basic interface passed to {@link HealthIndicator} instances allowing them to 
 * provide their view of {@link Health}
 */
public interface HealthIndicatorCallback {
	void inform(Health status);
}
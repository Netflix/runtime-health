package com.netflix.runtime.health.api;

public interface HealthIndicatorCallback {
	void complete(Health status);
}
package com.netflix.runtime.health.governator;

import com.google.inject.AbstractModule;
import com.netflix.runtime.health.core.HealthCheck;
import com.netflix.runtime.health.core.HealthIndicatorRegistry;

/**
 */
public class HealthCheckModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HealthIndicatorRegistry.class).to(AllHealthIndicatorRegistry.class);
        bind(HealthCheck.class).to(InjectorHealthCheck.class);
    }
}

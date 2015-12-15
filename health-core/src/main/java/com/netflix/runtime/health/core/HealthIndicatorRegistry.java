package com.netflix.runtime.health.core;

import java.util.Arrays;
import java.util.List;

import com.netflix.runtime.health.api.HealthIndicator;

/**
 * Registry of active HealthIndicator's. The HealthIndicator are used by {@link HealthCheck} to
 * determine the application health.
 * <p>
 * To create a curated list of {@link HealthIndicator}s create a binding to {@link HealthIndicatorRegistry} as follows:
 * <p>
 * <code>
 * {@literal @}Provides
 * {@literal @}Singleton
 * HealthIndicatorRegistry getHealthIndicatorRegistry({@literal @}Named("cpu") HealthIndicator cpuIndicator, {@literal @}Named("foo") HealthIndicator fooIndicator) {
 *    return HealthIndicatorRegistry.from(cpuIndicator, fooIndicator);
 * }
 * </code>
 * 
 * @author elandau
 */
public interface HealthIndicatorRegistry {
    /**
     * @return Return a list of all active health checks
     */
    List<HealthIndicator> getHealthIndicators();
    
    static HealthIndicatorRegistry from(List<HealthIndicator> healthChecks) {
        return new HealthIndicatorRegistry() {
                @Override
                public List<HealthIndicator> getHealthIndicators() {
                    return healthChecks;
                }
            };     
    }

    static HealthIndicatorRegistry from(HealthIndicator... healthChecks) {
        return from(Arrays.asList(healthChecks));
    }
}

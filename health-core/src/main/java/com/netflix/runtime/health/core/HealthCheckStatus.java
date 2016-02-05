package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.List;

import com.netflix.runtime.health.api.Health;

/**
 * Immutable status returned by {@link HealthCheckAggregator}.
 */
public class HealthCheckStatus {

    private final boolean isHealthy;

    private final List<Health> indicators;

    public HealthCheckStatus(boolean isHealthy, List<Health> indicators) {
        this.isHealthy = isHealthy;
        this.indicators = new ArrayList<>(indicators);
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public List<Health> getIndicators() {
        return indicators;
    }

    public static HealthCheckStatus create(boolean isHealthy, List<Health> indicators) {
        return new HealthCheckStatus(isHealthy, indicators);
    }
    
    @Override
    public String toString() {
        return "HealthCheckStatus[isHealthy=" + isHealthy + ", indicators=" + indicators + "]";
    }
}

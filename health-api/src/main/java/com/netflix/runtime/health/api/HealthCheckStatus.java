package com.netflix.runtime.health.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable status returned by {@link HealthCheckAggregator}.
 */
public class HealthCheckStatus {

    private final boolean isHealthy;

    private final List<Health> healthResults;

    public HealthCheckStatus(boolean isHealthy, List<Health> indicators) {
        this.isHealthy = isHealthy;
        this.healthResults = new ArrayList<>(indicators);
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public List<Health> getHealthResults() {
        return healthResults;
    }

    public static HealthCheckStatus create(boolean isHealthy, List<Health> indicators) {
        return new HealthCheckStatus(isHealthy, indicators);
    }
    
    @Override
    public String toString() {
        return "HealthCheckStatus[isHealthy=" + isHealthy + ", indicators=" + healthResults + "]";
    }
}

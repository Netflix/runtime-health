package com.netflix.runtime.health.core;

import com.netflix.runtime.health.api.HealthIndicatorStatus;

import java.util.Collections;
import java.util.List;

/**
 * Immutable status returned by {@link HealthCheck}.
 * 
 * @author elandau
 *
 */
public class HealthCheckStatus {

    public enum HealthState {
        Starting,
        Healthy,
        Unhealthy,
        OutOfService
    }

    private final HealthState state;

    private final List<HealthIndicatorStatus> indicators;

    public HealthCheckStatus(HealthState state, List<HealthIndicatorStatus> indicators) {
        this.state = state;
        this.indicators = indicators;
    }

    public HealthState getState() {
        return state;
    }

    public List<HealthIndicatorStatus> getIndicators() {
        return indicators;
    }

    public static HealthCheckStatus healthy(HealthState state) {
        return new HealthCheckStatus(state, Collections.emptyList());
    }
}

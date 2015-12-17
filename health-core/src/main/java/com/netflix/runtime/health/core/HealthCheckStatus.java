package com.netflix.runtime.health.core;

import com.netflix.runtime.health.api.HealthIndicatorStatus;

import java.util.Collections;
import java.util.List;

/**
 * Immutable status returned by {@link HealthCheck}.
 *
 * @author elandau
 */
public class HealthCheckStatus {

    private final boolean state;

    private final List<HealthIndicatorStatus> indicators;

    public HealthCheckStatus(boolean state, List<HealthIndicatorStatus> indicators) {
        this.state = state;
        this.indicators = indicators;
    }

    public boolean getState() {
        return state;
    }

    public List<HealthIndicatorStatus> getIndicators() {
        return indicators;
    }

    public static HealthCheckStatus healthy(boolean state) {
        return new HealthCheckStatus(state, Collections.emptyList());
    }
}

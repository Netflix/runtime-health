package com.netflix.runtime.health.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netflix.runtime.health.api.Health;

/**
 * Immutable status returned by {@link HealthCheckAggregator}.
 *
 * @author elandau
 */
public class HealthCheckStatus {

    private final boolean state;

    private final List<Health> indicators;

    public HealthCheckStatus(boolean state, List<Health> indicators) {
        this.state = state;
        this.indicators = new ArrayList<>(indicators);
    }

    public boolean getState() {
        return state;
    }

    public List<Health> getIndicators() {
        return indicators;
    }

    public static HealthCheckStatus healthy(boolean state) {
        return new HealthCheckStatus(state, Collections.emptyList());
    }
}

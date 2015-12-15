package com.netflix.runtime.health.governator;

import com.google.inject.Inject;
import com.netflix.governator.LifecycleManager;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;
import com.netflix.runtime.health.api.HealthIndicatorStatuses;
import com.netflix.runtime.health.core.HealthCheckStatus;
import com.netflix.runtime.health.core.HealthIndicatorRegistry;
import com.netflix.runtime.health.core.SimpleHealthCheck;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Determine the health of an application by combining the {@link LifecycleManager} state
 * with the {@link HealthIndicator}s tracked by the {@link HealthIndicatorRegistry}.
 *
 * @author elandau
 */
@Singleton
public class InjectorHealthCheck extends SimpleHealthCheck {

    private final LifecycleManager lifecycleManager;

    @Inject
    public InjectorHealthCheck(LifecycleManager lifecycleManager,
                               HealthIndicatorRegistry registry) {
        super(registry);
        this.lifecycleManager = lifecycleManager;
    }

    public CompletableFuture<HealthCheckStatus> check() {
        return super.check().thenApply(state -> {
            if (state.getState() != HealthCheckStatus.HealthState.Healthy) {
                return state;
            }
            HealthCheckStatus.HealthState effectiveState;
            switch (lifecycleManager.getState()) {
                case Starting:
                    effectiveState = HealthCheckStatus.HealthState.Starting;
                    break;
                case Started:
                    effectiveState = HealthCheckStatus.HealthState.Healthy;
                    break;
                case Done:
                case Stopped:
                    effectiveState = HealthCheckStatus.HealthState.OutOfService;
                    break;
                case Failed:
                    effectiveState = HealthCheckStatus.HealthState.Unhealthy;
                default:
                    effectiveState = HealthCheckStatus.HealthState.OutOfService;
                    break;
            }
            return new HealthCheckStatus(effectiveState, state.getIndicators());
        });
    }
}

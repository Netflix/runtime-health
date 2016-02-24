package com.netflix.runtime.health.core;

import com.netflix.governator.event.ApplicationEvent;
import com.netflix.governator.event.EventListener;
import com.netflix.runtime.health.api.HealthCheckStatus;

/***
 * An {@link ApplicationEvent} fired each team the overall health of an application changes.
 * See @{@link EventListener} for details on how to consume.
 */
public class HealthCheckStatusChangedEvent implements ApplicationEvent {
    
    private final HealthCheckStatus health;

    public HealthCheckStatusChangedEvent(HealthCheckStatus health) {
        this.health = health;
    }

    public HealthCheckStatus getHealth() {
        return health;
    }
    
}

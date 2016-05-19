package com.netflix.runtime.health.eureka;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.StatusChangeEvent;
import com.netflix.governator.event.ApplicationEvent;

public class EurekaStatusChangeEvent extends StatusChangeEvent implements ApplicationEvent {
    public EurekaStatusChangeEvent(StatusChangeEvent event) {
        this(event.getPreviousStatus(), event.getStatus());
    }

    public EurekaStatusChangeEvent(InstanceStatus previous, InstanceStatus current) {
        super(previous, current);
    }
}
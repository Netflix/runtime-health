package com.netflix.runtime.health.eureka;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.ApplicationInfoManager.StatusChangeListener;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.StatusChangeEvent;
import com.netflix.governator.event.ApplicationEvent;
import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.governator.event.ApplicationEventListener;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.core.HealthCheckStatusChangedEvent;

public class EurekaHealthStatusBridgeModule extends AbstractModule {

    @Override
    protected void configure() {
       bind(ApplicationEventListener.class).to(EurekaHealthStatusInformingApplicationEventListener.class).asEagerSingleton();
    }

    private static class EurekaHealthStatusInformingApplicationEventListener implements ApplicationEventListener<HealthCheckStatusChangedEvent> {
    
        private final ApplicationEventDispatcher eventDispatcher;
        private final ApplicationInfoManager applicationInfoManager;
        private final HealthCheckAggregator healthCheckAggregator;
        private final EurekaClient eurekaClient;
        
        @Inject
        public EurekaHealthStatusInformingApplicationEventListener(ApplicationEventDispatcher eventDispatcher,
                HealthCheckAggregator healthCheckAggregator, EurekaClient eurekaClient,
                ApplicationInfoManager applicationInfoManager) {
            this.eventDispatcher = eventDispatcher;
            this.applicationInfoManager = applicationInfoManager;
            this.eurekaClient = eurekaClient;
            this.healthCheckAggregator = healthCheckAggregator;
        }
    
        @PostConstruct
        public void init() throws InterruptedException, ExecutionException {
            applicationInfoManager.registerStatusChangeListener(new StatusChangeListener() {

                @Override
                public void notify(StatusChangeEvent statusChangeEvent) {
                    eventDispatcher.publishEvent(new DiscoveryStatusChangeEvent(statusChangeEvent));
                }

                @Override
                public String getId() {
                    return "governatorDiscoveryStatusEventBridge";
                }
            });

            eurekaClient.registerHealthCheck(new HealthCheckHandler() {
                @Override
                public InstanceStatus getStatus(InstanceStatus currentStatus) {
                    try {
                        return getInstanceStatusForHealth(healthCheckAggregator.check().get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    
        private static class DiscoveryStatusChangeEvent extends StatusChangeEvent implements ApplicationEvent {
            public DiscoveryStatusChangeEvent(StatusChangeEvent event) {
                this(event.getPreviousStatus(), event.getStatus());
            }
    
            public DiscoveryStatusChangeEvent(InstanceStatus previous, InstanceStatus current) {
                super(previous, current);
            }
        }
    
        @Override
        public void onEvent(HealthCheckStatusChangedEvent event) {
            applicationInfoManager.getInfo().setStatus(getInstanceStatusForHealth(event.getHealth()));
        }
    
        private InstanceStatus getInstanceStatusForHealth(HealthCheckStatus health) {
            if (health.isHealthy()) {
                return InstanceStatus.UP;
            } else {
                return InstanceStatus.DOWN;
            }
        }
    }
}

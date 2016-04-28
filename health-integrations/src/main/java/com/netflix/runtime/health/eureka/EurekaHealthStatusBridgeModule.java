/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.netflix.governator.spi.LifecycleListener;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.IndicatorFilter;
import com.netflix.runtime.health.core.HealthCheckStatusChangedEvent;
import com.netflix.runtime.health.status.ArchaiusHealthStatusFilterModule;

/**
 * Installing this module couples Eureka status (UP/DOWN/STARTING) to {@link HealthCheckStatus}. After injector creation, Eureka will be provided
 * with a {@link HealthCheckHandler} instance which delegates to
 * {@link HealthCheckAggregator}. A "healthy" status will report UP and
 * "unhealthy" will report DOWN in Eureka.
 * 
 * Please note that prior to injector creation being completed, Eureka will
 * remain at its default status of STARTING unless it is explicitly set
 * otherwise.
 */
public class EurekaHealthStatusBridgeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationEventListener.class).to(EurekaHealthStatusInformingApplicationEventListener.class).asEagerSingleton();
    }

    private static class EurekaHealthStatusInformingApplicationEventListener
            implements ApplicationEventListener<HealthCheckStatusChangedEvent>, LifecycleListener {

        @com.google.inject.Inject(optional = true)
        private ApplicationEventDispatcher eventDispatcher;
        @Inject
        private ApplicationInfoManager applicationInfoManager;
        @Inject
        private HealthCheckAggregator healthCheckAggregator;
        @Inject
        private EurekaClient eurekaClient;
        
        /***
         * See {@link ArchaiusHealthStatusFilterModule} for default implementation.
         */
        @com.google.inject.Inject(optional=true)
        private IndicatorFilter filter;

        @PostConstruct
        public void init() throws InterruptedException, ExecutionException {
            if (eventDispatcher != null) {
                applicationInfoManager.registerStatusChangeListener(new StatusChangeListener() {

                    @Override
                    public void notify(StatusChangeEvent statusChangeEvent) {
                        eventDispatcher.publishEvent(new EurekaStatusChangeEvent(statusChangeEvent));
                    }

                    @Override
                    public String getId() {
                        return "eurekaHealthStatusBridgeModuleStatusChangeListener";
                    }
                });
            }
        }

        @Override
        public void onStarted() {
            eurekaClient.registerHealthCheck(new HealthCheckHandler() {
                @Override
                public InstanceStatus getStatus(InstanceStatus currentStatus) {
                    try {
                        if(filter != null) {
                            return getInstanceStatusForHealth(healthCheckAggregator.check().get());
                        } else {
                            return getInstanceStatusForHealth(healthCheckAggregator.check().get());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        public void onStopped(Throwable error) {
        }

        private static class EurekaStatusChangeEvent extends StatusChangeEvent implements ApplicationEvent {
            public EurekaStatusChangeEvent(StatusChangeEvent event) {
                this(event.getPreviousStatus(), event.getStatus());
            }

            public EurekaStatusChangeEvent(InstanceStatus previous, InstanceStatus current) {
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
    
    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

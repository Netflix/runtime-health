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
import com.google.inject.Provider;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.ApplicationInfoManager.StatusChangeListener;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.StatusChangeEvent;
import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.governator.event.ApplicationEventListener;
import com.netflix.governator.spi.LifecycleListener;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.IndicatorMatcher;
import com.netflix.runtime.health.core.HealthCheckStatusChangedEvent;

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
        private Provider<ApplicationEventDispatcher> eventDispatcher;
        @Inject
        private Provider<ApplicationInfoManager> applicationInfoManager;
        @Inject
        private Provider<HealthCheckAggregator> healthCheckAggregator;
        @Inject
        private Provider<EurekaClient> eurekaClient;
        
        /***
         * See {@link com.netflix.runtime.health.status.ArchaiusHealthStatusFilterModule} for default implementation.
         */
        @com.google.inject.Inject(optional=true)
        private IndicatorMatcher matcher;

        @PostConstruct
        public void init() throws InterruptedException, ExecutionException {
            if (eventDispatcher != null) {
                applicationInfoManager.get().registerStatusChangeListener(new StatusChangeListener() {

                    @Override
                    public void notify(StatusChangeEvent statusChangeEvent) {
                        eventDispatcher.get().publishEvent(new EurekaStatusChangeEvent(statusChangeEvent));
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
            eurekaClient.get().registerHealthCheck(
                    new HealthAggregatorEurekaHealthCheckHandler(healthCheckAggregator.get(), matcher));
        }

        @Override
        public void onStopped(Throwable error) {
        }

        @Override
        public void onEvent(HealthCheckStatusChangedEvent event) {
            applicationInfoManager.get().setInstanceStatus(getInstanceStatusForHealth(event.getHealth()));
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

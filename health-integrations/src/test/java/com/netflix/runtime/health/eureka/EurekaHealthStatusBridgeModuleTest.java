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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.EurekaClient;
import com.netflix.governator.InjectorBuilder;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.core.HealthCheckStatusChangedEvent;

@RunWith(MockitoJUnitRunner.class)
public class EurekaHealthStatusBridgeModuleTest {
    
    Injector injector;
    @Mock ApplicationInfoManager infoManager;
    @Mock EurekaClient eurekaClient;
    @Mock HealthCheckAggregator healthCheckAggregator;
    @Captor ArgumentCaptor<HealthCheckStatusChangedEvent> healthStatusChangedEventCaptor;
    
    @Test
    public void testHealthCheckHandlerRegistered() {
        InjectorBuilder.fromModules(new EurekaHealthStatusBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(ApplicationInfoManager.class).toInstance(infoManager);
                bind(EurekaClient.class).toInstance(eurekaClient);
                bind(HealthCheckAggregator.class).toInstance(healthCheckAggregator);
            }
        }).createInjector();
        Mockito.verify(eurekaClient, Mockito.times(1)).registerHealthCheck(Mockito.any(HealthCheckHandler.class));
    }
}

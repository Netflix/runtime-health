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
package com.netflix.runtime.health.guice;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public class HealthModuleTest {

    static HealthIndicator healthy = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    };
    
    static HealthIndicator unhealthy = new HealthIndicator() {
        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.unhealthy().build());
        }
    };
    
    @Test
    public void testNoIndicators() throws InterruptedException, ExecutionException {
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule(), new ArchaiusModule()).createInjector();
        HealthCheckAggregator aggregator = injector.getInstance(HealthCheckAggregator.class);
        assertNotNull(aggregator);
        HealthCheckStatus healthCheckStatus = aggregator.check().get();
        assertTrue(healthCheckStatus.isHealthy());
        assertEquals(0, healthCheckStatus.getHealthResults().size());
    }
    
    @Test
    public void testMultipleIndicators() throws InterruptedException, ExecutionException {
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule(), new ArchaiusModule(), new AbstractModule() {            
            @Override
            protected void configure() {
                Multibinder<HealthIndicator> healthIndicatorBinder = Multibinder.newSetBinder(binder(), HealthIndicator.class);
                healthIndicatorBinder.addBinding().toInstance(healthy);
            }
        }, new AbstractModule() {            
            @Override
            protected void configure() {
                Multibinder<HealthIndicator> healthIndicatorBinder = Multibinder.newSetBinder(binder(), HealthIndicator.class);
                healthIndicatorBinder.addBinding().toInstance(unhealthy); 
            }
        }).createInjector();
        HealthCheckAggregator aggregator = injector.getInstance(HealthCheckAggregator.class);
        assertNotNull(aggregator);
        HealthCheckStatus healthCheckStatus = aggregator.check().get();
        assertFalse(healthCheckStatus.isHealthy());
        assertEquals(2, healthCheckStatus.getHealthResults().size());
    }
    
    @Test
    public void testConfiguringIndicatorsByExtendingHealthModule() throws InterruptedException, ExecutionException {
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule() {
            @Override
            protected void configureHealth() {
                bindAdditionalHealthIndicator().toInstance(healthy);
            }
        }, new ArchaiusModule()).createInjector();
        HealthCheckAggregator aggregator = injector.getInstance(HealthCheckAggregator.class);
        assertNotNull(aggregator);
        HealthCheckStatus healthCheckStatus = aggregator.check().get();
        assertTrue(healthCheckStatus.isHealthy());
        assertEquals(1, healthCheckStatus.getHealthResults().size());
    }
    
    @Test
    public void testMultipleInstancesOfHealthModuleInstalled() throws InterruptedException, ExecutionException {
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule() {
            @Override
            protected void configureHealth() {
                bindAdditionalHealthIndicator().toInstance(healthy);
            }
        }, new HealthModule() {
            @Override
            protected void configureHealth() {
                bindAdditionalHealthIndicator().toInstance(unhealthy);
            }
        }, new ArchaiusModule()).createInjector();
        HealthCheckAggregator aggregator = injector.getInstance(HealthCheckAggregator.class);
        assertNotNull(aggregator);
        HealthCheckStatus healthCheckStatus = aggregator.check().get();
        assertFalse(healthCheckStatus.isHealthy());
        assertEquals(2, healthCheckStatus.getHealthResults().size());
    }

}


package com.netflix.runtime.health.guice;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
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
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule()).createInjector();
        HealthCheckAggregator aggregator = injector.getInstance(HealthCheckAggregator.class);
        assertNotNull(aggregator);
        HealthCheckStatus healthCheckStatus = aggregator.check().get();
        assertTrue(healthCheckStatus.isHealthy());
        assertEquals(0, healthCheckStatus.getHealthResults().size());
    }
    
    @Test
    public void testMultipleIndicators() throws InterruptedException, ExecutionException {
        LifecycleInjector injector = InjectorBuilder.fromModules(new HealthModule(), new AbstractModule() {            
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

}


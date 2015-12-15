package com.netflix.runtime.health.governator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicators;
import com.netflix.runtime.health.core.HealthCheck;
import com.netflix.runtime.health.core.HealthCheckStatus;
import com.netflix.runtime.health.core.HealthCheckStatus.HealthState;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class HealthIndicatorTest {
    @Test
    public void failureOnNoDefinedHealthCheck() {
        LifecycleInjector injector = Governator.createInjector(new HealthCheckModule());

        InjectorHealthCheck hc = injector.getInstance(InjectorHealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(0, status.getIndicators().size());
    }

    @Test
    public void successWithSingleHealthCheck() {
        LifecycleInjector injector = Governator.createInjector(
                new HealthCheckModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
                    }
                });

        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }

    @Test
    public void successWithSingleAndNamedHealthCheck() {
        LifecycleInjector injector = Governator.createInjector(
                new HealthCheckModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
                        bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                    }
                });

        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());

        System.out.println(status.getIndicators());
    }

    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        LifecycleInjector injector = Governator.createInjector(
                new HealthCheckModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                    }
                });
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }

    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        LifecycleInjector injector = Governator.createInjector(
                new HealthCheckModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                        bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicators.alwaysUnhealthy("bar"));
                    }
                });

        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
}

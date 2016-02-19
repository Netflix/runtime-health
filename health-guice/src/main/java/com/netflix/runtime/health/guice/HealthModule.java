package com.netflix.runtime.health.guice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.governator.event.guava.GuavaApplicationEventModule;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.core.SimpleHealthCheckAggregator;
import com.netflix.runtime.health.core.caching.DefaultCachingHealthCheckAggregator;

public class HealthModule extends AbstractModule {

    private static final String CONFIG_PREFIX = "health.aggregator";

    @Provides
    @Singleton
    public HealthModuleConfiguration healthConfiguration(ConfigProxyFactory factory) {
        return factory.newProxy(HealthModuleConfiguration.class, CONFIG_PREFIX);
    }

    @Override
    protected void configure() {
        install(new GuavaApplicationEventModule());
        install(new ArchaiusModule());
        bind(HealthCheckAggregator.class).toProvider(HealthProvider.class).asEagerSingleton();
    }

    private static class HealthProvider implements Provider<HealthCheckAggregator> {

        @Inject(optional = true)
        private Set<HealthIndicator> indicators;

        @Inject
        private HealthModuleConfiguration config;

        @Inject
        private ApplicationEventDispatcher dispatcher;

        @Override
        public HealthCheckAggregator get() {
            if (indicators == null) {
                indicators = Collections.emptySet();
            }
            if (config.cacheHealthIndicators()) {
                return new DefaultCachingHealthCheckAggregator(new ArrayList<HealthIndicator>(indicators),
                        config.getCacheInterval(), config.getCacheIntervalUnits(), config.getAggregatorWaitInterval(),
                        config.getAggregatorWaitIntervalUnits(), dispatcher);
            } else {
                return new SimpleHealthCheckAggregator(new ArrayList<HealthIndicator>(indicators),
                        config.getAggregatorWaitInterval(), config.getAggregatorWaitIntervalUnits(), dispatcher);
            }
        }
    }
}

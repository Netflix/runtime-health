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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.governator.event.guava.GuavaApplicationEventModule;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.core.HealthCheckStatusChangedEvent;
import com.netflix.runtime.health.core.SimpleHealthCheckAggregator;
import com.netflix.runtime.health.core.caching.DefaultCachingHealthCheckAggregator;

/***
 * Guice module for installing runtime-health components. Installing this module
 * providers support for registered custom {@link HealthIndicator}s. {@link HealthCheckStatusChangedEvent}s
 * are dispatched each time the aggregated {@link HealthCheckStatus} for the application changes.
 * 
 * For configuration of caching and timeouts of {@link HealthIndicator}s, see {@link HealthAggregatorConfiguration}.
 * 
 * Custom {@link HealthIndicator}s may be registered as follows:
 * <code>
 * InjectorBuilder.fromModules(new HealthModule() {
 *      protected void configureHealth() {
 *          bindAdditionalHealthIndicator().to(MyCustomerIndicator.class);
 *      }
 * }).createInjector()
 * </code>
 * 
 */
public class HealthModule extends AbstractModule {

    @Override
    final protected void configure() {
        install(new InternalHealthModule());
        configureHealth();
    }

    /***
     * Override to provide custom {@link HealthIndicator}s
     */
    protected void configureHealth() {
    };
    
    final protected LinkedBindingBuilder<HealthIndicator> bindAdditionalHealthIndicator() {
        return Multibinder.newSetBinder(binder(), HealthIndicator.class).addBinding();
    }

    private final static class InternalHealthModule extends AbstractModule {
        
        @Provides
        @Singleton
        public HealthAggregatorConfiguration healthConfiguration(ConfigProxyFactory factory) {
            return factory.newProxy(HealthAggregatorConfiguration.class);
        }
        
        @Override
        protected void configure() {
            install(new GuavaApplicationEventModule());
            requireBinding(Key.get(ConfigProxyFactory.class));
            bind(HealthCheckAggregator.class).toProvider(HealthProvider.class).asEagerSingleton();
        }
        
        @Override
        public boolean equals(Object obj) {
            return getClass().equals(obj.getClass());
        }
        
        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        public String toString() {
            return "InternalHealthModule[]";
        }
    }
    
    private static class HealthProvider implements Provider<HealthCheckAggregator> {

        @Inject(optional = true)
        private Set<HealthIndicator> indicators;

        @Inject
        private HealthAggregatorConfiguration config;

        @Inject
        private ApplicationEventDispatcher dispatcher;

        @Override
        public HealthCheckAggregator get() {
            if (indicators == null) {
                indicators = Collections.emptySet();
            }
            if (config.cacheHealthIndicators()) {
                return new DefaultCachingHealthCheckAggregator(new ArrayList<HealthIndicator>(indicators),
                        config.getCacheIntervalInMillis(), TimeUnit.MILLISECONDS, config.getAggregatorWaitIntervalInMillis(),
                        TimeUnit.MILLISECONDS, dispatcher);
            } else {
                return new SimpleHealthCheckAggregator(new ArrayList<HealthIndicator>(indicators),
                        config.getAggregatorWaitIntervalInMillis(), TimeUnit.MILLISECONDS, dispatcher);
            }
        }
    }
}

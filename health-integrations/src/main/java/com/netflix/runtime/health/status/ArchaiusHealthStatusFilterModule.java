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
package com.netflix.runtime.health.status;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.IndicatorMatcher;
import com.netflix.runtime.health.api.IndicatorMatchers;
import com.netflix.runtime.health.eureka.EurekaHealthStatusBridgeModule;
import com.netflix.runtime.health.servlet.HealthStatusServlet;

/***
 * Installing this module will provide an implementation of {@link IndicatorMatcher} backed by Archaius2. 
 * This {@link IndicatorMatcher} will be used by both the {@link EurekaHealthStatusBridgeModule} and
 * the {@link HealthStatusServlet} to specify which {@link HealthIndicator}s will be used in 
 * determining the healthiness of your application. 
 */
public class ArchaiusHealthStatusFilterModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public HealthStatusInclusionConfiguration healthConfiguration(ConfigProxyFactory factory) {
        return factory.newProxy(HealthStatusInclusionConfiguration.class);
    }
    
    @Provides
    @Singleton
    public IndicatorMatcher IndicatorMatcher(HealthStatusInclusionConfiguration config) {
        return new ArchaiusDrivenStatusFilter(config);
    }

    private static class ArchaiusDrivenStatusFilter implements IndicatorMatcher {

        private final HealthStatusInclusionConfiguration config;

        public ArchaiusDrivenStatusFilter(HealthStatusInclusionConfiguration config) {
            this.config = config;
        }

        @Override
        public boolean matches(HealthIndicator indicator) {
            return IndicatorMatchers
                    .includes(config.includedIndicators())
                    .excludes(config.excludedIndicators()).build()
                    .matches(indicator);
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

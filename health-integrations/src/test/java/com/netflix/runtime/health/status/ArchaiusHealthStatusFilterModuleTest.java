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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Key;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.runtime.health.api.IndicatorFilter;

public class ArchaiusHealthStatusFilterModuleTest {

    private static class A implements HealthIndicator {
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
        
        @Override
        public String getName() {
            return "A";
        }
    }
    
    private static class B implements HealthIndicator {
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
        
        @Override
        public String getName() {
            return "B";
        }
    }
    
    private LifecycleInjector injector;
    private SettableConfig config;
    private IndicatorFilter filter;
   
    
    @Before
    public void init() {
        injector = InjectorBuilder.fromModules(new ArchaiusModule(), new ArchaiusHealthStatusFilterModule()).createInjector();
        config = injector.getInstance(Key.get(SettableConfig.class, RuntimeLayer.class));
        filter = injector.getInstance(IndicatorFilter.class);
    }

    @Test
    public void testDefault() {
        assertTrue(filter.matches(new A()));
        assertTrue(filter.matches(new B()));
    }
    
    @Test
    public void testInclusion() {
        config.setProperty("health.status.indicators.include", "A");
        assertTrue(filter.matches(new A()));
        assertFalse(filter.matches(new B()));
        
        config.setProperty("health.status.indicators.include", "B");
        assertFalse(filter.matches(new A()));
        assertTrue(filter.matches(new B()));
        
        config.setProperty("health.status.indicators.include", "A,B");
        assertTrue(filter.matches(new A()));
        assertTrue(filter.matches(new B()));
    }
    
    @Test
    public void testExclusion() {
        config.setProperty("health.status.indicators.exclude", "A");
        assertFalse(filter.matches(new A()));
        assertTrue(filter.matches(new B()));
        
        config.setProperty("health.status.indicators.exclude", "B");
        assertTrue(filter.matches(new A()));
        assertFalse(filter.matches(new B()));
        
        config.setProperty("health.status.indicators.exclude", "A,B");
        assertFalse(filter.matches(new A()));
        assertFalse(filter.matches(new B()));
    }
    
    @Test
    public void testStringSplittingProducesNoWeirdEffects() {
        config.setProperty("health.status.indicators.exclude", ",,A,,");
        assertFalse(filter.matches(new A()));
        assertTrue(filter.matches(new B()));
    }
}

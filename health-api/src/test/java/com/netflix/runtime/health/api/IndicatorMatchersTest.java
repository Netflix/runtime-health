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
package com.netflix.runtime.health.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IndicatorMatchersTest {

    private static class A implements HealthIndicator {
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    }
    
    private static class B implements HealthIndicator {
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    }
    
    private static class C implements HealthIndicator {
        public void check(HealthIndicatorCallback healthCallback) {
            healthCallback.inform(Health.healthy().build());
        }
    }
    
    @Test
    public void testEmpty() {
        assertTrue(IndicatorMatchers.build().matches(new A()));
    }
    
    @Test
    public void testInclude() {
        assertTrue(IndicatorMatchers.includes(A.class.getName(), B.class.getName()).build().matches(new A()));
        assertTrue(IndicatorMatchers.includes(A.class.getName(), B.class.getName()).build().matches(new B()));
        assertFalse(IndicatorMatchers.includes(A.class.getName(), B.class.getName()).build().matches(new C()));
    }
    
    @Test
    public void testAdditiveInclude() {
        assertTrue(IndicatorMatchers.includes(A.class.getName()).includes(B.class.getName()).build().matches(new A()));
        assertTrue(IndicatorMatchers.includes(A.class.getName()).includes(B.class.getName()).build().matches(new B()));
        assertFalse(IndicatorMatchers.includes(A.class.getName()).includes(B.class.getName()).build().matches(new C()));
    }
    
    @Test
    public void testExclude() {
        assertFalse(IndicatorMatchers.excludes(A.class.getName(), B.class.getName()).build().matches(new A()));
        assertFalse(IndicatorMatchers.excludes(A.class.getName(), B.class.getName()).build().matches(new B()));
        assertTrue(IndicatorMatchers.excludes(A.class.getName(), B.class.getName()).build().matches(new C()));
    }
    
    @Test
    public void testAdditiveExclude() {
        assertFalse(IndicatorMatchers.excludes(A.class.getName()).excludes(B.class.getName()).build().matches(new A()));
        assertFalse(IndicatorMatchers.excludes(A.class.getName()).excludes(B.class.getName()).build().matches(new B()));
        assertTrue(IndicatorMatchers.excludes(A.class.getName()).excludes(B.class.getName()).build().matches(new C()));
    }
    
    @Test
    public void testIncludesAndExcludes() {
        assertTrue(IndicatorMatchers.includes(A.class.getName()).excludes(B.class.getName()).build().matches(new A()));
        assertFalse(IndicatorMatchers.includes(A.class.getName()).excludes(B.class.getName()).build().matches(new B()));
        assertFalse(IndicatorMatchers.includes(A.class.getName()).excludes(B.class.getName()).build().matches(new C()));
    }
    
    @Test
    public void testExcludeBeatsInclude() {
        assertFalse(IndicatorMatchers.includes(A.class.getName()).excludes(A.class.getName()).build().matches(new A()));
    }
}




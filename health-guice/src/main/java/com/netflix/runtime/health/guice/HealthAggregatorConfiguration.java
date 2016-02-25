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

import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthIndicator;

@Configuration(prefix="health.aggregator")
public interface HealthAggregatorConfiguration {

    /***
     * Should health indicator results be cached between invocations.
     */
    @DefaultValue("true")
    boolean cacheHealthIndicators();
    
    /***
     * Number of milliseconds for which the {@link HealthCheckAggregator} should cache the response of
     * any {@link HealthIndicator}.
     */
    @DefaultValue("5000")
    long getCacheIntervalInMillis();
    
    /***
     * Number of milliseconds for which the {@link HealthCheckAggregator} should wait for the response
     * of any {@link HealthIndicator} before considering it as unhealthy and canceling invocation.
     */
    @DefaultValue("1000")
    long getAggregatorWaitIntervalInMillis();
    
}

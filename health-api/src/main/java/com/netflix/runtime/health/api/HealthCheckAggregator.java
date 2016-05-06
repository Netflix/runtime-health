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

import java.util.concurrent.CompletableFuture;

/**
 * The HealthCheckAggregator consolidates implementations of {@link HealthIndicator} and is responsible
 * for invoking each of them and returning a composite view of their status. 
 */
public interface HealthCheckAggregator {
    /**
     * Invoke all configured {@link HealthIndicator} instances and return the overall health.
     */
    CompletableFuture<HealthCheckStatus> check();
    
    /**
     * Invoke all configured {@link HealthIndicator} instances and return the overall health.
     * {@link HealthIndicator}s not matched by the provided {@link IndicatorMatcher} will be reported
     * as "suppressed" and will not have their {@link Health} considered as part of {@link HealthCheckStatus}.isHealthy(). 
     * 
     * This can be used to prevent a set of {@link HealthIndicator}s from marking your service as "unhealthy", while still
     * surfacing the information about their failure. 
     */
    CompletableFuture<HealthCheckStatus> check(IndicatorMatcher matcher);
}

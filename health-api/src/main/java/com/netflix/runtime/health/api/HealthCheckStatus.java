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

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable status returned by {@link HealthCheckAggregator}.
 */
public class HealthCheckStatus {

    private final boolean isHealthy;

    private final List<Health> healthResults;
    private final List<Health> suppressedHealthResults;

    public HealthCheckStatus(boolean isHealthy, List<Health> indicators) {
        this.isHealthy = isHealthy;
        this.healthResults = new ArrayList<>(indicators);
        this.suppressedHealthResults = new ArrayList<>();
    }
    
    public HealthCheckStatus(boolean isHealthy, List<Health> indicators, List<Health> suppressedIndicators) {
        this.isHealthy = isHealthy;
        this.healthResults = new ArrayList<>(indicators);
        this.suppressedHealthResults = new ArrayList<>(suppressedIndicators);
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Health results returned by instances of {@link HealthIndicator} and not suppressed by an {@link IndicatorFilter}.
     */
    public List<Health> getHealthResults() {
        return healthResults;
    }
    
    /**
     * Health results returned by instances of {@link HealthIndicator} but suppressed by an {@link IndicatorFilter}.
     */
    public List<Health> getSuppressedHealthResults() {
        return suppressedHealthResults;
    }

    public static HealthCheckStatus create(boolean isHealthy, List<Health> indicators) {
        return new HealthCheckStatus(isHealthy, indicators);
    }
    
    public static HealthCheckStatus create(boolean isHealthy, List<Health> indicators, List<Health> suppressedIndicators) {
        return new HealthCheckStatus(isHealthy, indicators, suppressedIndicators);
    }
    
    @Override
    public String toString() {
        return "HealthCheckStatus[isHealthy=" + isHealthy + ", indicators=" + healthResults + ", + suppressedIndicators=" + suppressedHealthResults + "]";
    }
}

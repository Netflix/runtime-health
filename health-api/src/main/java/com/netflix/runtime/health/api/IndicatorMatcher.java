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

/**
 * A matcher used when invoking {@link HealthCheckAggregator}.check() to explicitly include/exclude which
 * indicators should be used when calculating the healthiness of the application. See {@link IndicatorMatchers}
 * for creating IndicatorMatchers programmatically. 
 */
public interface IndicatorMatcher {

    boolean matches(HealthIndicator indicator);
}

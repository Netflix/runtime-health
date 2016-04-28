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
 * Basic interface for defining health indication logic.  0 or more HealthIndicators are used to determine 
 * the application health. HealthIndicators are tracked by a {@link HealthCheckAggregator}
 * where the default implementation uses all registered HealthIndicators.  
 * 
 * HealthIndicator can inject types that are to be consulted for health indication, call out to shell 
 * scripts or call a remote service.  
 * 
 * To register a health indicator,
 * <code>
 * InjectorBuilder.fromModules(new HealthModule() {
 *      protected void configureHealth() {
 *          bindAdditionalHealthIndicator().to(MyCustomerIndicator.class);
 *      }
 * }).createInjector()
 * </code>
 *  
 * Here is a sample health indicator implementation. 
 * 
 * <code>
 * public class MyHealthIndicator implements HealthIndicator {
 *     {@literal @}Inject
 *     public MyHealthIndicator(MyService service) {
 *         this.service = service;
 *     }
 *     
 *     {@literal @}Inject
 *     
 *     public void check(HealthIndicatorCallback healthCallback) {
 *          if (service.getErrorRate() {@literal >} 0.1) {
 *              healthCallback.inform(Health.unhealthy()
 *              			  				.withDetails("errorRate", service.getErrorRate()));
 *          }
 *          else {
 *              healthCallback.inform(Health.healthy());
 *          }
 *     }
 * }
 * </code>
 * 
 * @author elandau
 */
public interface HealthIndicator {
    /**
     * Inform the provided {@link HealthIndicatorCallback} of the {@link Health}.
     * 
     * Implementations should catch exceptions and return a status of unhealthy to provide customized messages. 
     * Uncaught exceptions will be captured by the default implementation of {@link HealthCheckAggregator} and 
     * returned as an unhealthy status automatically. 
     * 
     * Implementations of {@link HealthCheckAggregator} will also handle threading and timeouts for implementations 
     * of {@link HealthIndicator}. Each {@link HealthIndicator} will be run in its own thread with a timeout. 
     * Implementations should not spawn additional threads (or at least take responsibility for killing them).
     * Timeouts will result in an unhealthy status being returned for any slow {@link HealthIndicator}
     * with a status message indicating that it has timed out. 
     */
    void check(HealthIndicatorCallback healthCallback);

    /**
     * Name used in displaying and filtering (see {@link IndicatorFilter}) HealthIndicators. 
     */
    default String getName() {
        return this.getClass().getName();
    }

}

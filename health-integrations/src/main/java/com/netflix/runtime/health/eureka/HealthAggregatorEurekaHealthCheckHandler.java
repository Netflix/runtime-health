package com.netflix.runtime.health.eureka;

import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.IndicatorMatcher;

/**
 * A Eureka HealthCheckHandler instance which consults a HealthAggregator for
 * its status. This may be registered with a EurekaClient instance as follows:
 * 
 * <pre>
 * {@literal @}Inject
 * EurekaClient eurekaClient;
 * {@literal @}Inject
 * HealthCheckAggregator healthCheckAggregator;
 * {@literal @}Inject
 * IndicatorMatcher matcher;
 * 
 * {@literal @}PostConstruct
 * public void init() {
 *      eurekaClient.registerHealthCheck(
                    new HealthAggregatorEurekaHealthCheckHandler(healthCheckAggregator, matcher));
 * }
 * </pre>
 */
public class HealthAggregatorEurekaHealthCheckHandler implements HealthCheckHandler {

    private final HealthCheckAggregator healthCheckAggregator;
    private final IndicatorMatcher matcher;
    
    public HealthAggregatorEurekaHealthCheckHandler(HealthCheckAggregator healthCheckAggregator, IndicatorMatcher matcher) {
        this.healthCheckAggregator = healthCheckAggregator;
        this.matcher = matcher;
    }

    @Override
    public InstanceStatus getStatus(InstanceStatus currentStatus) {
        try {
            if (matcher != null) {
                return getInstanceStatusForHealth(healthCheckAggregator.check(matcher).get());
            } else {
                return getInstanceStatusForHealth(healthCheckAggregator.check().get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InstanceStatus getInstanceStatusForHealth(HealthCheckStatus health) {
        if (health.isHealthy()) {
            return InstanceStatus.UP;
        } else {
            return InstanceStatus.DOWN;
        }
    }
}
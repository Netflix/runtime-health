package com.netflix.runtime.health.core;

import com.netflix.runtime.health.api.HealthIndicator;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class EmptyHealthIndicatorRegistry implements HealthIndicatorRegistry {
    @Override
    public List<HealthIndicator> getHealthIndicators() {
        return Collections.emptyList();
    }
}

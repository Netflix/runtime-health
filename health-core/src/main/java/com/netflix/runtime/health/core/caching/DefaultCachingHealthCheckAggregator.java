package com.netflix.runtime.health.core.caching;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.core.SimpleHealthCheckAggregator;

public class DefaultCachingHealthCheckAggregator extends SimpleHealthCheckAggregator {
	
	public DefaultCachingHealthCheckAggregator(List<HealthIndicator> indicators, long cacheInterval, TimeUnit cacheIntervalUnits, long aggregatorWaitInterval, TimeUnit aggregatorWaitUnits) {
		super(indicators.stream()
				.map(delegate -> CachingHealthIndicator.wrap(delegate, cacheInterval, cacheIntervalUnits))
				.collect(Collectors.toList()), aggregatorWaitInterval, aggregatorWaitUnits);
	}

}

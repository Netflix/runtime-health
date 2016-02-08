package com.netflix.runtime.health.core.caching;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

public class CachingHealthIndicatorTest {

	HealthIndicator testHealthIndicator;
	AtomicLong realCount;
	AtomicLong cachedCount;

	@Before
	public void init() {
		realCount = new AtomicLong();
		cachedCount = new AtomicLong();
		this.testHealthIndicator = new HealthIndicator() {
			@Override
			public void check(HealthIndicatorCallback c) {
				realCount.incrementAndGet();
				c.inform(Health.healthy().build());
			}
		};
	}

	@Test
	public void testWithoutCaching() {
		for (int x = 0; x < 10; x++) {
			testHealthIndicator.check(h -> cachedCount.incrementAndGet());
		}
		assertEquals(10, realCount.get());
		assertEquals(10, cachedCount.get());
	}

	@Test
	public void testWithCaching() {
		CachingHealthIndicator cachedIndicator = CachingHealthIndicator.wrap(testHealthIndicator, 100, TimeUnit.MILLISECONDS);
		for (int x = 0; x < 10; x++) {
			cachedIndicator.check(h -> cachedCount.incrementAndGet());
		}
		assertEquals(1, realCount.get());
		assertEquals(10, cachedCount.get());
	}

	@Test
	public void testWithCachingAndExpiry() throws InterruptedException {
		CachingHealthIndicator cachedIndicator = CachingHealthIndicator.wrap(testHealthIndicator, 100, TimeUnit.MILLISECONDS);
		for (int x = 0; x < 10; x++) {
			cachedIndicator.check(h -> {
				cachedCount.incrementAndGet();
			});
			Thread.sleep(25);
		}
		assertEquals(2, realCount.get());
		assertEquals(10, cachedCount.get());
	}

}

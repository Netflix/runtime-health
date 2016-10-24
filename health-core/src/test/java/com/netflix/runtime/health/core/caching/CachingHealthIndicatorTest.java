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
package com.netflix.runtime.health.core.caching;

import static org.junit.Assert.*;

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
			long startTime = System.nanoTime();
			@Override
			public void check(HealthIndicatorCallback c) {
				System.out.println("real call: " + (System.nanoTime() - startTime));
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
			cachedIndicator.check(h -> {
				cachedCount.incrementAndGet();	
			});
		}
		assertEquals(1, realCount.get());
		assertEquals(10, cachedCount.get());
		
	}

	@Test
	public void testWithCachingAndExpiry() throws InterruptedException {
		CachingHealthIndicator cachedIndicator = CachingHealthIndicator.wrap(testHealthIndicator, 100, TimeUnit.MILLISECONDS);
		long startTime = System.nanoTime();
		// first real call expected at +000ms, second at +100ms, third at +200ms
		for (int x = 0; x < 10; x++) {
			cachedIndicator.check(h -> {
				System.out.println("cached call: " + (System.nanoTime() - startTime));
				cachedCount.incrementAndGet();
			});
			Thread.sleep(25);
		}
		assertEquals(10, cachedCount.get());
		assertEquals(3, realCount.get()); 
	}
	
    @Test
    public void testValueActuallyCached() {
        CachingHealthIndicator cachedIndicator = CachingHealthIndicator.wrap(testHealthIndicator, 100, TimeUnit.MILLISECONDS);
        CallbackShim firstHealth = new CallbackShim();
        CallbackShim cachedHeath = new CallbackShim();
        cachedIndicator.check(firstHealth);
        cachedIndicator.check(cachedHeath);
        
        assertEquals(firstHealth.status.isHealthy(), cachedHeath.status.isHealthy());
        assertEquals(true, cachedHeath.status.getDetails().get("cached"));

    }


    private class CallbackShim implements HealthIndicatorCallback {

        public Health status;

        @Override
        public void inform(Health status) {
            this.status = status;
        }

    }
}


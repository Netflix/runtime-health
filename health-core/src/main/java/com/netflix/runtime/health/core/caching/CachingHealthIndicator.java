package com.netflix.runtime.health.core.caching;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

/**
 * HealthIndicator wrapper implementation that caches the response
 *
 * @author elandau
 *
 */
public class CachingHealthIndicator implements HealthIndicator {
    private final AtomicLong      expireTime = new AtomicLong(0);
    private final long            interval;
    private final HealthIndicator delegate;
    private final AtomicBoolean   busy = new AtomicBoolean();
    private volatile Health health;
    
    private CachingHealthIndicator(HealthIndicator delegate, long interval, TimeUnit units) {
        this.delegate = delegate;
        this.interval = TimeUnit.NANOSECONDS.convert(interval, units);
    }
    
    @Override
    public void check(HealthIndicatorCallback callback) {
        long lastExpireTime = this.expireTime.get();
        long currentTime  = System.nanoTime();
        
        if (currentTime > lastExpireTime + interval) {
            long expireTime = currentTime + interval;
            if (this.expireTime.compareAndSet(lastExpireTime, expireTime)) {
            	if (busy.compareAndSet(false, true)) {
            		try {
            			delegate.check(callback);
            		}
            		finally {
            			busy.set(false);
            		}
            	}
            }
        }
        else {
        	callback.complete(health);
        }
    }

    public static CachingHealthIndicator wrap(HealthIndicator delegate, long interval, TimeUnit units) {
        return new CachingHealthIndicator(delegate, interval, units);
    }

}

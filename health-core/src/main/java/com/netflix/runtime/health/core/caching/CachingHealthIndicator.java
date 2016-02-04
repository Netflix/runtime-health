package com.netflix.runtime.health.core.caching;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;
import com.netflix.runtime.health.api.HealthIndicatorStatus;

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
    private volatile HealthIndicatorStatus status;
    
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
                delegate.check(callback);
            }
        }
        else {
        	callback.complete(status);
        }
    }

    public static CachingHealthIndicator cache(HealthIndicator delegate, long interval, TimeUnit units) {
        return new CachingHealthIndicator(delegate, interval, units);
    }

}

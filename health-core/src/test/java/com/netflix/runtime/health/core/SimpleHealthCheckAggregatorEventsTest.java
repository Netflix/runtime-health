package com.netflix.runtime.health.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.netflix.governator.event.ApplicationEventDispatcher;
import com.netflix.runtime.health.api.Health;
import com.netflix.runtime.health.api.HealthCheckStatus;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorCallback;

@RunWith(MockitoJUnitRunner.class)
public class SimpleHealthCheckAggregatorEventsTest {

    @Mock ApplicationEventDispatcher dispatcher;
    HealthIndicator changing;

    @Before
    public void setup() {
        changing = new HealthIndicator() {
            int counter = 0;

            @Override
            public void check(HealthIndicatorCallback healthCallback) {
                if (counter++ == 0)
                    healthCallback.inform(Health.healthy().build());
                else
                    healthCallback.inform(Health.unhealthy().build());
            }
        };
    }

    @Test(timeout = 100)
    public void testChangingHealthSendsFirstEventWhenNoListeners() throws Exception {
        SimpleHealthCheckAggregator aggregator = new SimpleHealthCheckAggregator(Collections.emptyList(), 100, TimeUnit.SECONDS,dispatcher);
        HealthCheckStatus aggregatedHealth = aggregator.check().get();
        assertTrue(aggregatedHealth.isHealthy());
        assertEquals(0, aggregatedHealth.getHealthResults().size());
        Thread.sleep(10);
        Mockito.verify(dispatcher, Mockito.times(1)).publishEvent(Mockito.any());
    }

    @Test(timeout = 100)
    public void testChangingHealthSendsEvent() throws Exception {
        SimpleHealthCheckAggregator aggregator = new SimpleHealthCheckAggregator(Arrays.asList(changing), 100, TimeUnit.SECONDS,dispatcher);
        HealthCheckStatus aggregatedHealth = aggregator.check().get();
        assertTrue(aggregatedHealth.isHealthy());
        assertEquals(1, aggregatedHealth.getHealthResults().size());
        Thread.sleep(10);
        Mockito.verify(dispatcher, Mockito.times(1)).publishEvent(Mockito.any());
        aggregator.check().get();
        Thread.sleep(10);
        Mockito.verify(dispatcher, Mockito.times(2)).publishEvent(Mockito.any());
        aggregator.check().get();
        Thread.sleep(10);
        Mockito.verifyNoMoreInteractions(dispatcher);
    }

}

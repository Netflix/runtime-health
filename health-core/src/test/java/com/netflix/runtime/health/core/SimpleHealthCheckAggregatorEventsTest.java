package com.netflix.runtime.health.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
    
    SimpleHealthCheckAggregator aggregator;
    @Mock ApplicationEventDispatcher dispatcher;
    
    static HealthIndicator changing = new HealthIndicator() {
        int counter = 0;
         @Override
         public void check(HealthIndicatorCallback healthCallback) {
             if(counter++ == 0)
                 healthCallback.inform(Health.healthy().build());
             else
                 healthCallback.inform(Health.unhealthy().build());
         }
     };
     
     @Test(timeout=100)
     public void testChangingHealthSendsEvent() throws Exception {
         aggregator = new SimpleHealthCheckAggregator(Arrays.asList(changing), 1, TimeUnit.SECONDS, dispatcher);
         HealthCheckStatus aggregatedHealth = aggregator.check().get();
         assertTrue(aggregatedHealth.isHealthy());
         assertEquals(1, aggregatedHealth.getHealthResults().size());
         Mockito.verify(dispatcher, Mockito.times(1)).publishEvent(Mockito.any());
         aggregator.check().get();
         Mockito.verify(dispatcher, Mockito.times(2)).publishEvent(Mockito.any());
         aggregator.check().get();
         Mockito.verifyNoMoreInteractions(dispatcher);
     }

}

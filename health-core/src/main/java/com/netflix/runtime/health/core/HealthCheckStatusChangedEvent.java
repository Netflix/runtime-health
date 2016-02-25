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
package com.netflix.runtime.health.core;

import com.netflix.governator.event.ApplicationEvent;
import com.netflix.governator.event.EventListener;
import com.netflix.runtime.health.api.HealthCheckStatus;

/***
 * An {@link ApplicationEvent} fired each team the overall health of an application changes.
 * See @{@link EventListener} for details on how to consume.
 */
public class HealthCheckStatusChangedEvent implements ApplicationEvent {
    
    private final HealthCheckStatus health;

    public HealthCheckStatusChangedEvent(HealthCheckStatus health) {
        this.health = health;
    }

    public HealthCheckStatus getHealth() {
        return health;
    }
    
}

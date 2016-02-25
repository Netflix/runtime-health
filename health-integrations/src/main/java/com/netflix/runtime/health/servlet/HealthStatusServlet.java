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
package com.netflix.runtime.health.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.netflix.runtime.health.api.HealthCheckAggregator;
import com.netflix.runtime.health.api.HealthCheckStatus;

@Singleton
public final class HealthStatusServlet extends HttpServlet {
    
    private static final long serialVersionUID = -6518168654611266480L;
    private final HealthCheckAggregator healthCheckAggregator;

    @Inject
    public HealthStatusServlet(HealthCheckAggregator healthCheckAggregator) {
        this.healthCheckAggregator = healthCheckAggregator;
    }
    
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        HealthCheckStatus health;
        try {
            health = this.healthCheckAggregator.check().get();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        
        if(health.isHealthy()) {
            resp.setStatus(200);
        }
        else {
            resp.setStatus(500);
        }
        String content = health.toString();
        resp.setContentLength(content.length());
        resp.setContentType("text/plain");
        resp.getWriter().print(content);
    }

}

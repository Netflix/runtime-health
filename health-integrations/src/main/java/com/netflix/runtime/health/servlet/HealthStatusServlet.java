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

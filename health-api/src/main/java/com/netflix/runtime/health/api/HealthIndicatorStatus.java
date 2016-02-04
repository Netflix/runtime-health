package com.netflix.runtime.health.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable health check instance returned from a {@link HealthIndicator}
 * 
 * See {@link HealthIndicatorStatus} for utility methods to create HealthIndicatorStatus objects
 * @author elandau
 */
public final class HealthIndicatorStatus {
	
	private Map<String, Object> details;
	private boolean isHealthy;
	
	public HealthIndicatorStatus(boolean isHealthy) {
		this.isHealthy=isHealthy;
		this.details = new LinkedHashMap<>();
	}
	
	public HealthIndicatorStatus(boolean isHealthy, Map<String, Object> details) {
		this.isHealthy = isHealthy;
		this.details = details;
	}
	
    HealthIndicatorStatus(HealthIndicatorStatus status) {
		this.isHealthy = status.isHealthy();
		this.details = status.getDetails();
	}

	/**
     * @return Map of named attributes that provide additional information regarding the health.
     * For example, a CPU health check may return Unhealthy with attribute "usage"="90%"
     */
    public Map<String, Object> getDetails() {
    	return this.details;
    }
    
    /**
     * @return True if healthy or false otherwise.
     */
    public boolean isHealthy() {
    	return this.isHealthy;
    }

	public String getErrorMessage() {
		if(getDetails().get("error") != null)
			return getDetails().get("error").toString();
		else
			return null;
	}
    
}

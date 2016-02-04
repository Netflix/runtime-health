
package com.netflix.runtime.health.api;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Health {
	
	static final String ERROR_KEY = "error";
	private final Map<String, Object> details;
	private final boolean isHealthy;
	
	private Health(boolean isHealthy) {
		this.isHealthy=isHealthy;
		this.details = new LinkedHashMap<>();
	}
	
	private Health(boolean isHealthy, Map<String, Object> details) {
		this.isHealthy = isHealthy;
		this.details = new LinkedHashMap<>(details);
	}
	
	private Health(Health health) {
		this.isHealthy = health.isHealthy();
		this.details = health.getDetails();
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
		if(getDetails().get(ERROR_KEY) != null)
			return getDetails().get(ERROR_KEY).toString();
		else
			return null;
	}

	/**
	 * Create a new {@link Builder} instance with a {@link Health} of "healthy".
	 * @return a new {@link Builder} instance
	 */
	public static Builder healthy() {
		return health(new Health(true));
	}

	/**
	 * Create a new {@link Builder} instance with a {@link Health} of "unhealthy"
	 * which includes the provided exception details.
	 * @param ex the exception
	 * @return a new {@link Builder} instance
	 */
	public static Builder unhealthy(Throwable ex) {
		return unhealthy().withException(ex);
	}

	/**
	 * Create a new {@link Builder} instance with a {@link Health} of "unhealthy".
	 * @return a new {@link Builder} instance
	 */
	public static Builder unhealthy() {
		return health(new Health(false));
	}

	/**
	 * Create a new {@link Builder} instance with a specific {@link Health}.
	 * @param health the health
	 * @return a new {@link Builder} instance
	 */
	private static Builder health(Health health) {
		return new Builder(health);
	}

	/**
	 * Builder for creating immutable {@link Health} instances.
	 */
	public static class Builder {

		private final boolean isHealthy;
		private final Map<String, Object> details;

		/**
		 * Create new Builder instance using a {@link Health}
		 * @param health the {@link Health} to use
		 */
		private Builder(Health health) {
			assertNotNull(health, "Health must not be null");
			this.isHealthy = health.isHealthy();
			this.details = health.getDetails();
		}

		/**
		 * Record detail for given {@link Exception}.
		 * @param ex the exception
		 * @return this {@link Builder} instance
		 */
		public Builder withException(Throwable ex) {
			assertNotNull(ex, "Exception must not be null");
			return withDetail(ERROR_KEY, ex.getClass().getName() + ": " + ex.getMessage());
		}

		/**
		 * Record detail using {@code key} and {@code value}.
		 * @param key the detail key
		 * @param data the detail data
		 * @return this {@link Builder} instance
		 */
		public Builder withDetail(String key, Object data) {
			assertNotNull(key, "Key must not be null");
			assertNotNull(data, "Data must not be null");
			this.details.put(key, data);
			return this;
		}

		/**
		 * Create a new {@link Health} from the provided information. 
		 * @return a new {@link Health} instance
		 */
		public Health build() {
			return new Health(this.isHealthy, this.details);
		}
	}
	
	private static void assertNotNull(Object test, String message) {
		if(test == null){
			throw new IllegalArgumentException(message);
		}
	}

}
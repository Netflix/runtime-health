
package com.netflix.runtime.health.api;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Health {

	/**
	 * Create a new {@link Builder} instance with a {@link HealthIndicatorStatus} of "healthy".
	 * @return a new {@link Builder} instance
	 */
	public static Builder healthy() {
		return status(new HealthIndicatorStatus(true));
	}

	/**
	 * Create a new {@link Builder} instance with a {@link HealthIndicatorStatus} of "unhealthy"
	 * which includes the provided exception details.
	 * @param ex the exception
	 * @return a new {@link Builder} instance
	 */
	public static Builder unhealthy(Throwable ex) {
		return unhealthy().withException(ex);
	}

	/**
	 * Create a new {@link Builder} instance with a {@link HealthIndicatorStatus} of "unhealthy".
	 * @return a new {@link Builder} instance
	 */
	public static Builder unhealthy() {
		return status(new HealthIndicatorStatus(false));
	}

	/**
	 * Create a new {@link Builder} instance with a specific {@link HealthIndicatorStatus}.
	 * @param status the status
	 * @return a new {@link Builder} instance
	 */
	private static Builder status(HealthIndicatorStatus status) {
		return new Builder(status);
	}

	/**
	 * Builder for creating immutable {@link HealthIndicatorStatus} instances.
	 */
	public static class Builder {

		private boolean isHealthy;
		private Map<String, Object> details;

		/**
		 * Create new Builder instance.
		 */
		public Builder() {
			this.isHealthy = false; 
			this.details = new LinkedHashMap<String, Object>();
		}

		/**
		 * Create new Builder instance using a {@link HealthIndicatorStatus}
		 * @param status the {@link HealthIndicatorStatus} to use
		 */
		private Builder(HealthIndicatorStatus status) {
			assertNotNull(status, "HealthIndicatorStatus must not be null");
			this.isHealthy = status.isHealthy();
			this.details = new LinkedHashMap<String, Object>();
		}

		/**
		 * Record detail for given {@link Exception}.
		 * @param ex the exception
		 * @return this {@link Builder} instance
		 */
		public Builder withException(Throwable ex) {
			assertNotNull(ex, "Exception must not be null");
			return withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
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
		 * Create a new {@link HealthIndicatorStatus} from the provided information. 
		 * @return a new {@link HealthIndicatorStatus} instance
		 */
		public HealthIndicatorStatus build() {
			return new HealthIndicatorStatus(this.isHealthy, this.details);
		}
	}
	
	private static void assertNotNull(Object test, String message) {
		if(test == null){
			throw new IllegalArgumentException(message);
		}
	}

}

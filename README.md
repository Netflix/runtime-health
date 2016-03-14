Health check
----------------
Instance health is an important aspect of any cloud ready application. It is used for service discovery as well as bad instance termination. Through HealthCheck API an application can expose a REST endpoint for external monitoring to ping for health status or integrate with Eureka for service discovery registration based on instance health state. An instance can be in one of 2 lifecycle states: Healthy and Unhealthy. 

The HealthCheck API is broken up into two abstractions to allow for maximum customization.  
* HealthIndicator - boolean health indicator for a specific application features/aspect.  For example, CPU usage.
* HealthCheckAggregator - combines indicators to derive a meaningful health state. Responsible caching of HealthIndicators, basic error handling and HealthIndicator timeouts. Also enriches Health information with details such as the indicator name and whether or not a Health entry was cached. 

### Artifacts
Runtime-health is published as the following artifacts:
```
# api package for libraries
com.netflix.runtime:health-api

# core implementation of runtime-health for applications and services
com.netflix.runtime:health-core

# guice integration with module HealthModule() and netflix archaius2 integration for configuration
com.netflix.runtime:health-guice

# integration with netflix eureka and bridging of health state and eureka status
com.netflix.runtime:health-integrations
```

### Using HealthCheck in a Jersey resource
```java
    @Path("/health")
    public class HealthCheckResource {
        @Inject
        public HealthCheckResource(HealthCheckAggregator healthCheck) {
            this.healthCheck = healthCheck;
        }

        @GET
        public HealthCheckStatus doCheck() {
            return healthCheck.check().get();
        }
    }
```

### Custom health check
To create a custom health indicator simply implement HealthIndicator, inject any objects that are needed to determine the health state, and implement you logic in check().  Note that check returns a future so that the healthcheck system can implement a timeout.  The check() implementation is therefore expected to be well behaved and NOT block.

```java
    public class MyHealthIndicator implements HealthIndicator {
        @Inject
        public MyHealthIndicator(MyService service) {
            this.service = service;
        }

        @Override
        public void check(HealthIndicatorCallback healthCallback) {
            if (service.getErrorRate() > 0.1) {
                healthCallback.inform(Health.unhealthy().withDetails("errorRate", service.getErrorRate()));
            }
            else {
                healthCallback.inform(Health.healthy());
            }
        }
    }
```

To register a HealthIndicator simply provide it when installing HealthModule.  It will automatically be picked up by the default HealthCheckAggregator
```java
 InjectorBuilder.fromModules(new HealthModule() {
      protected void configureHealth() {
          bindAdditionalHealthIndicator().to(MyHealthIndicator.class);
          bindAdditionalHealthIndicator().to(AnotherHealthIndicator.class);  //e.g. if you have a second indicator defined
      }
 }).createInjector()
```

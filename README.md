Health check
----------------
Instance health is an important aspect of any cloud ready application. It is used for service discovery as well as bad instance termination. Through HealthCheck API an application can expose a REST endpoint for external monitoring to ping for health status or integrate with Eureka for service discovery registration based on instance health state. An instance can be in one of 4 lifecycle states: Starting, Running, Stopping and Stopped. HealthCheck state varies slightly in that it combines these application lifecycle states with the instance health to provide the following states: Starting, Healthy, Unhealthy or OutOfService.   

* Starting - the application is healthy but not done bootstrapping
* Healthy - the application finished bootstrapping and is functioning properly
* Unhealthy - the application either failed bootstrapping or is not functioning properly
* OutOfService - the application has been shut down

The HealthCheck API is broken up into several abstractions to allow for maximum customization.  It's important to understand these abstractions.
* HealthIndicator - boolean health indicator for a specific application features/aspect.  For example, CPU usage.
* HealthIndicatorRegistry - registry of all health indicators to consider for health check.   The default HealthIndicatorRegistry ANDs all bound HealthIndicator (MapBinding, Multibinding, Qualified Binding).  Altenatively and application may manually construct a HealthIndicatorRegistry from a curated set of HealthIndicators.
* HealthCheck - combines application lifecycle + indicators to derive a meaningful health state

### Using HealthCheck in a Jersey resource
```java
@Path("/health")
public class HealthCheckResource {
    @Inject
    public HealthCheckResource(HealthCheck healthCheck) {
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
public class MyHealthIndicator extends AbstractHealthIndicator {
    @Inject
    public MyHealthIndicator(MyService service) {
        this.service = service;
    }
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        if (service.getErrorRate() > 0.1) {
            return CompletableFuture.completedFuture(healthy(getName()));
        }
        else {
             return CompletableFuture.completedFuture(healthy(getName()));
        }
    }
 
    @Override
    public String getName() {
        return "MyService";
    }
}
```

To enable the HealthIndicator simply register it as a set binding.  It will automatically be picked up by the default HealthIndicatorRegistry
```java
Multbindings.newSetBinder(binder()).addBinding().to(MyHealthIndicator.class);
```
### Curated health check registry
TBD 

### Configuration based health indicator
TBD

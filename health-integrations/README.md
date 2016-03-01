Health Integrations
----------------
Integration of runtime-health with netflix eureka. This package provides bridging of health state and eureka status as a registered HealthCheckHandler for EurekaClient.
To use:
```java
InjectorBuilder
        .fromModules(
                // ... other modules ...
                new HealthModule(),
                new EurekaHealthStatusBridgeModule(),
               // ... other modules ...
        )
        .createInjector();
```

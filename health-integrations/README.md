# Health Integrations
----------------
### Eureka
Integration of runtime-health with netflix eureka. This package provides bridging of health state and eureka status as a registered HealthCheckHandler for EurekaClient.
The HealthCheckHandler registration with EurekaClient will not occur until after the Injector is created.

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

### Archaius2-backed IndicatorFilter
Filtering of which HealthIndicators will be used in communicating the health status of the application is provided via an [Archaius2](https://github.com/Netflix/archaius/tree/2.x) implementation of [IndicatorFilter](https://github.com/Netflix/runtime-health/blob/addIndicatorFilterSupport/health-api/src/main/java/com/netflix/runtime/health/api/IndicatorFilter.java).

```
InjectorBuilder
    .fromModules(
        // ... other modules ...
        new HealthModule(),
        new EurekaHealthStatusBridgeModule(),
        new ArchaiusHealthStatusFilterModule(),
        // ... other modules ...
    )
    .createInjector();
```

Indicators can now be included/excluded by setting the following properties in Archaius2.
```
health.status.indicators.include=com.myproject.MyFilter
health.status.indicators.exclude=com.myproject.DontIncludeThisFilter
```




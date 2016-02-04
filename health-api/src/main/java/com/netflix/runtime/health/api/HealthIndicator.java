package com.netflix.runtime.health.api;

/**
 * Basic interface for defining health indication logic.  0 or more HealthIndicators are used to determine 
 * the application health. HealthIndicators are tracked by a {@link HealthIndicatorRegistry}
 * where the default implementation uses all HealthIndicators registered as a set multibinding.  
 * 
 * HealthIndicator can inject types that are to be consulted for health indication, call out to shell 
 * scripts or call a remote service.  
 * 
 * To register a health indicator,
 * <code>
 * Multbindings.newSetBinder(binder()).addBinding().to(MyHealthIndicator.class);
 * </code>
 *  
 * Here is a sample health indicator implementation. 
 * 
 * <code>
 * public class MyHealthIndicator implements HealthIndicator {
 *     {@literal @}Inject
 *     public MyHealthIndicator(MyService service) {
 *         this.service = service;
 *     }
 *     
 *     {@literal @}Inject
 *     
 *     public CompletableFuture{@literal <}HealthIndicatorStatus{@literal >} check() {
 *          if (service.getErrorRate() {@literal >} 0.1) {
 *              return CompletableFuture.completedFuture(HealthIndicators.unhealthy(getName()));
 *          }
 *          else {
 *              return CompletableFuture.completedFuture(HealthIndicators.healthy(getName()));
 *          }
 *     }
 * }
 * </code>
 * 
 * @author elandau
 */
public interface HealthIndicator {
    /**
     * Perform the health check asynchronously.
     * @return Future of health status result
     */
    void check(HealthIndicatorCallback health);

}

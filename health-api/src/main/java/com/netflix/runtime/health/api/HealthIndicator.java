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
 *     public CompletableFuture{@literal <}HealthIndicatorStatus{@literal >} check(HealthIndicatorCallback healthCallback) {
 *          if (service.getErrorRate() {@literal >} 0.1) {
 *              healthCallback.inform(Health.unhealthy()
 *              			  				.withDetails("errorRate", service.getErrorRate()));
 *          }
 *          else {
 *              healthCallback.inform(Health.healthy());
 *          }
 *     }
 * }
 * </code>
 * 
 * @author elandau
 */
public interface HealthIndicator {
    /**
     * Inform the provided {@link HealthIndicatorCallback} of the {@link Health}
     */
    void check(HealthIndicatorCallback healthCallback);

}

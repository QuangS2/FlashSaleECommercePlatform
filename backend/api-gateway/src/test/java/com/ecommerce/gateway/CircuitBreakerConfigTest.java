package com.ecommerce.gateway;

import com.ecommerce.gateway.config.CustomCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import static org.assertj.core.api.Assertions.assertThat;

public class CircuitBreakerConfigTest {

    @Test
    @DisplayName("Test 1: defaultCustomizer - Configures Reactive Resilience4J Circuit Breaker correctly")
    public void testDefaultCustomizer() {
        CustomCircuitBreakerConfig config = new CustomCircuitBreakerConfig();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer = config.defaultCustomizer();

        assertThat(customizer).isNotNull();

        CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry tlRegistry = TimeLimiterRegistry.ofDefaults();
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory(cbRegistry, tlRegistry);
        customizer.customize(factory);

        assertThat(factory.create("testCircuitBreaker")).isNotNull();
    }
}

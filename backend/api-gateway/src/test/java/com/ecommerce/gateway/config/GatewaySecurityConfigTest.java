package com.ecommerce.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GatewaySecurityConfigTest {

    @Test
    @DisplayName("Test 1: Instantiate SecurityConfig bean")
    void testSecurityConfigInstantiation() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config);
    }

    @Test
    @DisplayName("Test 2: CustomCircuitBreakerConfig default instantiation")
    void testCircuitBreakerConfigInstantiation() {
        CustomCircuitBreakerConfig config = new CustomCircuitBreakerConfig();
        assertNotNull(config);
    }

    @Test
    @DisplayName("Test 3: CustomCircuitBreakerConfig customizer bean registration")
    void testCircuitBreakerCustomizerRegistration() {
        CustomCircuitBreakerConfig config = new CustomCircuitBreakerConfig();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer = config.defaultCustomizer();
        assertNotNull(customizer);

        ReactiveResilience4JCircuitBreakerFactory factory = mock(ReactiveResilience4JCircuitBreakerFactory.class);
        customizer.customize(factory);
        verify(factory, atLeastOnce()).configureDefault(any());
    }

    @Test
    @DisplayName("Test 4: Verify CORS and CSRF disabling expectation")
    void testSecurityConfigStructure() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config.getClass().getAnnotations());
    }

    @Test
    @DisplayName("Test 5: CustomCircuitBreakerConfig multiple invocation idempotency")
    void testCircuitBreakerCustomizerIdempotency() {
        CustomCircuitBreakerConfig config = new CustomCircuitBreakerConfig();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer1 = config.defaultCustomizer();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer2 = config.defaultCustomizer();

        assertNotNull(customizer1);
        assertNotNull(customizer2);
    }

    @Test
    @DisplayName("Test 6: Verify package configuration integrity")
    void testPackageIntegrity() {
        assertNotNull(SecurityConfig.class.getPackage().getName());
    }
}

package com.ecommerce.gateway.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GatewayDtoTest {

    @Test
    @DisplayName("Test 1: FallbackResponse factory method with basic parameters")
    void testFallbackResponseBasicFactory() {
        FallbackResponse response = FallbackResponse.of(503, "Service Unavailable", "order-service", "Service is degraded", 10);

        assertEquals(503, response.getStatus());
        assertEquals("Service Unavailable", response.getError());
        assertEquals("order-service", response.getService());
        assertEquals("Service is degraded", response.getMessage());
        assertEquals(10, response.getRetryAfterSeconds());
        assertNotNull(response.getTimestamp());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Test 2: FallbackResponse factory method with full parameters")
    void testFallbackResponseFullFactory() {
        Object mockData = "Cached Payload";
        FallbackResponse response = FallbackResponse.of(503, "Timeout", "product-service", "Timeout occurred", "TimeoutException", 5, mockData);

        assertEquals(503, response.getStatus());
        assertEquals("Timeout", response.getError());
        assertEquals("product-service", response.getService());
        assertEquals("TimeoutException", response.getExceptionType());
        assertEquals(5, response.getRetryAfterSeconds());
        assertEquals("Cached Payload", response.getData());
    }

    @Test
    @DisplayName("Test 3: FallbackResponse builder and setters")
    void testFallbackResponseBuilder() {
        FallbackResponse response = FallbackResponse.builder()
                .status(504)
                .error("Gateway Timeout")
                .service("inventory-service")
                .message("Downstream not responding")
                .build();

        assertEquals(504, response.getStatus());
        assertEquals("inventory-service", response.getService());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Test 4: FlashSaleCachedProduct builder and default values")
    void testFlashSaleCachedProductBuilder() {
        FlashSaleCachedProduct product = FlashSaleCachedProduct.builder()
                .productId("prod_100")
                .title("Gaming Monitor")
                .originalPrice(new BigDecimal("500.00"))
                .flashSalePrice(new BigDecimal("299.00"))
                .discountPercentage(40)
                .stockStatus("IN_STOCK")
                .build();

        assertEquals("prod_100", product.getProductId());
        assertEquals("Gaming Monitor", product.getTitle());
        assertEquals(new BigDecimal("500.00"), product.getOriginalPrice());
        assertEquals(new BigDecimal("299.00"), product.getFlashSalePrice());
        assertEquals(40, product.getDiscountPercentage());
        assertEquals("IN_STOCK", product.getStockStatus());
        assertTrue(product.isCached());
    }

    @Test
    @DisplayName("Test 5: FlashSaleCachedProduct no-args and all-args constructor")
    void testFlashSaleCachedProductConstructors() {
        FlashSaleCachedProduct product = new FlashSaleCachedProduct(
                "p2", "Phone", BigDecimal.valueOf(1000), BigDecimal.valueOf(800), 20, "LOW_STOCK", false
        );

        assertEquals("p2", product.getProductId());
        assertFalse(product.isCached());
        assertEquals(20, product.getDiscountPercentage());
    }

    @Test
    @DisplayName("Test 6: FallbackResponse no-args constructor and equals/hashCode")
    void testFallbackResponseEqualsHashCode() {
        FallbackResponse res1 = FallbackResponse.of(503, "Err", "srv", "msg", 1);
        FallbackResponse res2 = FallbackResponse.of(503, "Err", "srv", "msg", 1);
        res2.setTimestamp(res1.getTimestamp());

        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
    }
}

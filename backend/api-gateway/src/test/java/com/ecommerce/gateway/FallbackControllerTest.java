package com.ecommerce.gateway;

import com.ecommerce.gateway.controller.FallbackController;
import com.ecommerce.gateway.dto.FallbackResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class FallbackControllerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        FallbackController controller = new FallbackController();
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    @DisplayName("Test 1: GET /fallback/products - Returns 503 and product-service degraded response")
    public void testProductFallback() {
        webTestClient.get()
                .uri("/fallback/products")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(FallbackResponse.class)
                .value(response -> {
                    assertThat(response.getService()).isEqualTo("product-service");
                    assertThat(response.getStatus()).isEqualTo(503);
                    assertThat(response.getRetryAfterSeconds()).isEqualTo(10);
                    assertThat(response.getMessage()).contains("Dịch vụ sản phẩm");
                });
    }

    @Test
    @DisplayName("Test 2: GET /fallback/orders - Returns 503 and order-service overload response")
    public void testOrderFallback() {
        webTestClient.get()
                .uri("/fallback/orders")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(FallbackResponse.class)
                .value(response -> {
                    assertThat(response.getService()).isEqualTo("order-service");
                    assertThat(response.getStatus()).isEqualTo(503);
                    assertThat(response.getRetryAfterSeconds()).isEqualTo(10);
                    assertThat(response.getMessage()).contains("Hệ thống đặt hàng");
                });
    }

    @Test
    @DisplayName("Test 3: GET /fallback/inventory - Returns 503 and inventory-service unreachable response")
    public void testInventoryFallback() {
        webTestClient.get()
                .uri("/fallback/inventory")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(FallbackResponse.class)
                .value(response -> {
                    assertThat(response.getService()).isEqualTo("inventory-service");
                    assertThat(response.getStatus()).isEqualTo(503);
                    assertThat(response.getMessage()).contains("kho hàng");
                });
    }

    @Test
    @DisplayName("Test 4: GET /fallback/payments - Returns 503 and payment warning response")
    public void testPaymentFallback() {
        webTestClient.get()
                .uri("/fallback/payments")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(FallbackResponse.class)
                .value(response -> {
                    assertThat(response.getService()).isEqualTo("payment-service");
                    assertThat(response.getStatus()).isEqualTo(503);
                    assertThat(response.getMessage()).contains("Cổng thanh toán");
                    assertThat(response.getMessage()).contains("Giao dịch chưa bị trừ tiền");
                });
    }

    @Test
    @DisplayName("Test 5: GET /fallback/default - Returns 503 and gateway timeout response")
    public void testDefaultFallback() {
        webTestClient.get()
                .uri("/fallback/default")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(FallbackResponse.class)
                .value(response -> {
                    assertThat(response.getService()).isEqualTo("api-gateway");
                    assertThat(response.getError()).isEqualTo("GATEWAY_TIMEOUT");
                });
    }
}

package com.ecommerce.gateway.controller;

import com.ecommerce.gateway.dto.FallbackResponse;
import com.ecommerce.gateway.dto.FlashSaleCachedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/products")
    public Mono<ResponseEntity<FallbackResponse>> productFallback(ServerWebExchange exchange) {
        String exceptionType = extractException(exchange);
        log.warn("[GATEWAY FALLBACK] Kích hoạt Fallback cho Product Service. Lỗi gốc: {}", exceptionType);

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "product-service",
                "Dịch vụ sản phẩm đang quá tải hoặc tạm thời bảo trì. Quý khách vui lòng thử lại sau giây lát.",
                exceptionType,
                10,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Endpoint Phục hồi Suy thoái Mềm (Graceful Degradation): Trả về danh sách sản phẩm từ Cache khẩn cấp
     * khi product-service gặp sự cố trong đợt Flash Sale.
     */
    @GetMapping("/flashsale-cached")
    public Mono<ResponseEntity<FallbackResponse>> flashsaleCachedFallback() {
        log.info("[GATEWAY GRACEFUL DEGRADATION] Phục vụ danh mục Flash Sale từ Cache khẩn cấp của Gateway");

        List<FlashSaleCachedProduct> emergencyCatalog = List.of(
                FlashSaleCachedProduct.builder()
                        .productId("PROD-IPHONE-15-FLASH")
                        .title("iPhone 15 Pro Max 256GB - Flash Sale Edition")
                        .originalPrice(new BigDecimal("34990000"))
                        .flashSalePrice(new BigDecimal("29990000"))
                        .discountPercentage(14)
                        .stockStatus("IN_STOCK")
                        .cached(true)
                        .build(),
                FlashSaleCachedProduct.builder()
                        .productId("PROD-MACBOOK-M3-FLASH")
                        .title("MacBook Pro M3 Pro 18GB/512GB Space Black")
                        .originalPrice(new BigDecimal("49990000"))
                        .flashSalePrice(new BigDecimal("43990000"))
                        .discountPercentage(12)
                        .stockStatus("LOW_STOCK")
                        .cached(true)
                        .build()
        );

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.OK.value(),
                "GRACEFUL_DEGRADATION",
                "product-service",
                "Hệ thống đang phục vụ danh mục Flash Sale từ bộ nhớ đệm khẩn cấp. Một số tính năng cập nhật thời gian thực có thể bị trễ.",
                "ServiceDegradedException",
                5,
                emergencyCatalog
        );
        return Mono.just(ResponseEntity.ok(response));
    }

    @RequestMapping("/orders")
    public Mono<ResponseEntity<FallbackResponse>> orderFallback(ServerWebExchange exchange) {
        String exceptionType = extractException(exchange);
        log.warn("[GATEWAY FALLBACK] Kích hoạt Fallback cho Order Service. Lỗi gốc: {}", exceptionType);

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "order-service",
                "Hệ thống đặt hàng đang nhận lưu lượng truy cập đột biến trong phiên Flash Sale. Vui lòng bấm thử lại sau ít giây.",
                exceptionType,
                10,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/inventory")
    public Mono<ResponseEntity<FallbackResponse>> inventoryFallback(ServerWebExchange exchange) {
        String exceptionType = extractException(exchange);
        log.warn("[GATEWAY FALLBACK] Kích hoạt Fallback cho Inventory Service. Lỗi gốc: {}", exceptionType);

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "inventory-service",
                "Dịch vụ kiểm tra kho hàng đang tạm thời không phản hồi. Xin vui lòng thử lại.",
                exceptionType,
                10,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/payments")
    public Mono<ResponseEntity<FallbackResponse>> paymentFallback(ServerWebExchange exchange) {
        String exceptionType = extractException(exchange);
        log.warn("[GATEWAY FALLBACK] Kích hoạt Fallback cho Payment Service. Lỗi gốc: {}", exceptionType);

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "payment-service",
                "Cổng thanh toán đang xử lý chậm hoặc bảo trì đột xuất. Giao dịch chưa bị trừ tiền, xin vui lòng kiểm tra lại sau.",
                exceptionType,
                10,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/default")
    public Mono<ResponseEntity<FallbackResponse>> defaultFallback(ServerWebExchange exchange) {
        String exceptionType = extractException(exchange);
        log.warn("[GATEWAY FALLBACK] Kích hoạt Fallback mặc định. Lỗi gốc: {}", exceptionType);

        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "GATEWAY_TIMEOUT",
                "api-gateway",
                "Dịch vụ máy chủ tạm thời không phản hồi kịp thời. Yêu cầu của bạn đã được ngắt mạch an toàn.",
                exceptionType,
                10,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    private String extractException(ServerWebExchange exchange) {
        if (exchange == null) {
            return "UnknownCircuitBreakerException";
        }
        Object attr = exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        if (attr instanceof Throwable t) {
            return t.getClass().getSimpleName() + ": " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
        }
        String header = exchange.getRequest().getHeaders().getFirst("Execution-Exception-Type");
        return header != null ? header : "CallNotPermittedException (CircuitBreaker OPEN)";
    }
}

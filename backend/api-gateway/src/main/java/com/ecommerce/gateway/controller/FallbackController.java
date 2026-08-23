package com.ecommerce.gateway.controller;

import com.ecommerce.gateway.dto.FallbackResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/products")
    public Mono<ResponseEntity<FallbackResponse>> productFallback() {
        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "product-service",
                "Dịch vụ sản phẩm đang quá tải hoặc tạm thời bảo trì. Quý khách vui lòng thử lại sau giây lát.",
                10
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/orders")
    public Mono<ResponseEntity<FallbackResponse>> orderFallback() {
        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "order-service",
                "Hệ thống đặt hàng đang nhận lưu lượng truy cập đột biến trong phiên Flash Sale. Vui lòng bấm thử lại sau ít giây.",
                10
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/inventory")
    public Mono<ResponseEntity<FallbackResponse>> inventoryFallback() {
        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "inventory-service",
                "Dịch vụ kiểm tra kho hàng đang tạm thời không phản hồi. Xin vui lòng thử lại.",
                10
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/payments")
    public Mono<ResponseEntity<FallbackResponse>> paymentFallback() {
        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE",
                "payment-service",
                "Cổng thanh toán đang xử lý chậm hoặc bảo trì đột xuất. Giao dịch chưa bị trừ tiền, xin vui lòng kiểm tra lại sau.",
                10
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/default")
    public Mono<ResponseEntity<FallbackResponse>> defaultFallback() {
        FallbackResponse response = FallbackResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "GATEWAY_TIMEOUT",
                "api-gateway",
                "Dịch vụ máy chủ tạm thời không phản hồi kịp thời. Yêu cầu của bạn đã được ngắt mạch an toàn.",
                10
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}

package com.ecommerce.gateway.filter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Đoạn mã 3 trong Báo cáo:
 * Bộ lọc toàn cục HeaderSanitizationFilter làm sạch Header và chuyển tiếp danh tính JWT.
 * Ngăn chặn tấn công BOLA/IDOR bằng cách strip các header nhạy cảm do client giả mạo
 * và chỉ gắn identity chính chủ sau khi thẩm định JWT RS256.
 */
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {

    private final ObjectProvider<ReactiveJwtDecoder> jwtDecoderProvider;

    public HeaderSanitizationFilter(ObjectProvider<ReactiveJwtDecoder> jwtDecoderProvider) {
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Làm sạch các header giả mạo từ máy khách bên ngoài
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Roles");
                    h.remove("X-User-Email");
                }).build();

        // 2. Thẩm định JWT RS256 và gắn identity chính chủ vào header chuyển tiếp
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        ReactiveJwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();

        if (jwtDecoder != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtDecoder.decode(token)
                    .flatMap(jwt -> {
                        List<String> roles = jwt.getClaimAsStringList("roles");
                        String rolesStr = roles != null ? String.join(",", roles) : "";
                        String email = jwt.getClaimAsString("email");

                        ServerHttpRequest.Builder builder = request.mutate()
                                .header("X-User-Id", jwt.getSubject())
                                .header("X-User-Roles", rolesStr);

                        if (email != null && !email.isBlank()) {
                            builder.header("X-User-Email", email);
                        }

                        ServerHttpRequest mutated = builder.build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    })
                    .onErrorResume(ex -> chain.filter(exchange.mutate().request(request).build()));
        }

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

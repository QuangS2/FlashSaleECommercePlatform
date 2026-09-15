package com.ecommerce.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderSanitizationFilterTest {

    @Mock
    private ReactiveJwtDecoder jwtDecoder;

    @Mock
    private ObjectProvider<ReactiveJwtDecoder> jwtDecoderProvider;

    @Mock
    private GatewayFilterChain chain;

    private HeaderSanitizationFilter filter;

    @BeforeEach
    void setUp() {
        lenient().when(jwtDecoderProvider.getIfAvailable()).thenReturn(jwtDecoder);
        filter = new HeaderSanitizationFilter(jwtDecoderProvider);
        lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Test 1: Strip spoofed headers when no Authorization header is present")
    void testStripSpoofedHeadersWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header("X-User-Id", "attacker-id")
                .header("X-User-Roles", "ROLE_ADMIN")
                .header("X-User-Email", "admin@spoofed.com")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertNull(headers.getFirst("X-User-Id"));
        assertNull(headers.getFirst("X-User-Roles"));
        assertNull(headers.getFirst("X-User-Email"));
    }

    @Test
    @DisplayName("Test 2: Decode valid JWT and attach verified identity headers")
    void testDecodeValidJwtAndAttachIdentity() {
        Jwt jwt = new Jwt(
                "token-abc",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "user-genuine-123",
                        "roles", List.of("ROLE_CUSTOMER", "ROLE_BUYER"),
                        "email", "user@flashsale.com"
                )
        );

        when(jwtDecoder.decode("token-abc")).thenReturn(Mono.just(jwt));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-abc")
                .header("X-User-Id", "spoofed-user")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertEquals("user-genuine-123", headers.getFirst("X-User-Id"));
        assertEquals("ROLE_CUSTOMER,ROLE_BUYER", headers.getFirst("X-User-Roles"));
        assertEquals("user@flashsale.com", headers.getFirst("X-User-Email"));
    }

    @Test
    @DisplayName("Test 3: Gracefully handle invalid JWT with onErrorResume and forward sanitized request")
    void testInvalidJwtFailsGracefully() {
        when(jwtDecoder.decode("bad-token")).thenReturn(Mono.error(new BadJwtException("Invalid signature")));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
                .header("X-User-Id", "spoofed")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertNull(headers.getFirst("X-User-Id"));
    }

    @Test
    @DisplayName("Test 4: Verify filter order is HIGHEST_PRECEDENCE")
    void testFilterOrder() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, filter.getOrder());
    }

    @Test
    @DisplayName("Test 5: Handle non-Bearer Authorization header")
    void testNonBearerAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .header("X-User-Id", "spoofed")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertNull(captor.getValue().getRequest().getHeaders().getFirst("X-User-Id"));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    @DisplayName("Test 6: Handle empty or whitespace Authorization header")
    void testEmptyAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    @DisplayName("Test 7: Valid JWT with null roles and null email")
    void testJwtWithNullRolesAndNullEmail() {
        Jwt jwt = new Jwt(
                "token-null-claims",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "user-minimal-999")
        );

        when(jwtDecoder.decode("token-null-claims")).thenReturn(Mono.just(jwt));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-null-claims")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertEquals("user-minimal-999", headers.getFirst("X-User-Id"));
        assertEquals("", headers.getFirst("X-User-Roles"));
        assertNull(headers.getFirst("X-User-Email"));
    }

    @Test
    @DisplayName("Test 8: Handle null ReactiveJwtDecoder gracefully")
    void testNullJwtDecoderProvider() {
        when(jwtDecoderProvider.getIfAvailable()).thenReturn(null);
        HeaderSanitizationFilter nullFilter = new HeaderSanitizationFilter(jwtDecoderProvider);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .header("X-User-Id", "spoofed")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(nullFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertNull(captor.getValue().getRequest().getHeaders().getFirst("X-User-Id"));
    }

    @Test
    @DisplayName("Test 9: Valid JWT with blank email claim is ignored")
    void testJwtWithBlankEmailClaim() {
        Jwt jwt = new Jwt(
                "token-blank-email",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "user-blank-email",
                        "roles", List.of("ROLE_USER"),
                        "email", "   "
                )
        );

        when(jwtDecoder.decode("token-blank-email")).thenReturn(Mono.just(jwt));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-blank-email")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertEquals("user-blank-email", headers.getFirst("X-User-Id"));
        assertEquals("ROLE_USER", headers.getFirst("X-User-Roles"));
        assertNull(headers.getFirst("X-User-Email"));
    }

    @Test
    @DisplayName("Test 10: Downstream chain error propagated properly")
    void testDownstreamChainErrorPropagated() {
        Jwt jwt = new Jwt(
                "token-err",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "user-err")
        );

        when(jwtDecoder.decode("token-err")).thenReturn(Mono.just(jwt));
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.error(new RuntimeException("Downstream timeout")));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-err")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Test 11: Spoofed headers stripped even when path is public")
    void testPublicPathSpoofedHeadersStripped() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products/public")
                .header("X-User-Id", "fake-admin")
                .header("X-User-Roles", "SUPERUSER")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        HttpHeaders headers = captor.getValue().getRequest().getHeaders();
        assertNull(headers.getFirst("X-User-Id"));
        assertNull(headers.getFirst("X-User-Roles"));
    }
}

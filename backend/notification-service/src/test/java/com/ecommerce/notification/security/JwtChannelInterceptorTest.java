package com.ecommerce.notification.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtChannelInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private ObjectProvider<JwtDecoder> jwtDecoderProvider;

    @Mock
    private MessageChannel messageChannel;

    private JwtChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        lenient().when(jwtDecoderProvider.getIfAvailable()).thenReturn(jwtDecoder);
        interceptor = new JwtChannelInterceptor(jwtDecoderProvider);
    }

    @Test
    @DisplayName("Test 1: STOMP CONNECT with valid JWT sets UserPrincipal")
    void testConnectValidJwt() {
        Jwt jwt = new Jwt(
                "token-valid",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "cust-999", "roles", List.of("CUSTOMER"))
        );
        when(jwtDecoder.decode("token-valid")).thenReturn(jwt);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "Bearer token-valid");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);

        StompHeaderAccessor resAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resAccessor.getUser());
        assertEquals("cust-999", resAccessor.getUser().getName());
    }

    @Test
    @DisplayName("Test 2: STOMP CONNECT with invalid JWT throws AccessDeniedException")
    void testConnectInvalidJwtThrows() {
        when(jwtDecoder.decode("bad-token")).thenThrow(new BadJwtException("Signature expired"));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer bad-token");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    @DisplayName("Test 3: STOMP SUBSCRIBE to own user queue succeeds")
    void testSubscribeOwnQueueSucceeds() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/cust-999/queue/order-status");
        accessor.setUser(new UserPrincipal("cust-999", List.of("CUSTOMER")));
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test 4: STOMP SUBSCRIBE to another user's queue throws AccessDeniedException")
    void testSubscribeOtherUserQueueThrows() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/victim-user/queue/order-status");
        accessor.setUser(new UserPrincipal("attacker-user", List.of("CUSTOMER")));
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    @DisplayName("Test 5: STOMP SUBSCRIBE to public topic succeeds without restriction")
    void testSubscribePublicTopicSucceeds() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/flash-sale/prod-iphone");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test 6: STOMP CONNECT without Authorization header passes through")
    void testConnectWithoutAuthHeaderPasses() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    @DisplayName("Test 7: STOMP CONNECT with non-Bearer auth is ignored")
    void testConnectWithNonBearerAuthIgnored() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Token abc123xyz");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    @DisplayName("Test 8: STOMP SEND or DISCONNECT commands pass through untouched")
    void testNonConnectOrSubscribePassesThrough() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test 9: STOMP SUBSCRIBE with null destination passes through")
    void testSubscribeWithNullDestination() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(null);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertNotNull(result);
    }
}

package com.ecommerce.notification.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * Đoạn mã 4 trong Báo cáo:
 * Bộ chặn JwtChannelInterceptor thẩm định kết nối và kiểm soát quyền đăng ký kênh STOMP.
 * Ngăn chặn nghe lén thông báo cá nhân tại /user/queue/order-status.
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public JwtChannelInterceptor(ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearerToken(accessor);
            JwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();
            if (token != null && jwtDecoder != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    UserPrincipal principal = new UserPrincipal(jwt.getSubject(), extractRoles(jwt));
                    if (accessor.isMutable()) {
                        accessor.setUser(principal);
                    } else {
                        StompHeaderAccessor mutableAccessor = StompHeaderAccessor.wrap(message);
                        mutableAccessor.setUser(principal);
                        return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                    }
                } catch (AccessDeniedException ade) {
                    throw ade;
                } catch (Exception ex) {
                    throw new AccessDeniedException("JWT token không hợp lệ hoặc đã hết hạn", ex);
                }
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // Kiểm soát quyền đăng ký kênh: chỉ cho phép kênh công khai hoặc hàng đợi cá nhân
            String dest = accessor.getDestination();
            Principal user = accessor.getUser();
            String userName = user != null ? user.getName() : null;

            if (dest != null && dest.startsWith("/user/") && (userName == null || !dest.contains(userName))) {
                throw new AccessDeniedException("Truy cập trái phép kênh thông báo người dùng khác");
            }
        }

        return message;
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null ? roles : Collections.emptyList();
    }
}

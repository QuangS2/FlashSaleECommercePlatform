package com.ecommerce.cart.infrastructure.adapter.out.redis;

import com.ecommerce.cart.domain.entity.Cart;
import com.ecommerce.cart.domain.port.out.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisCartAdapter implements CartRepositoryPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(7); // TTL 7 ngày theo Bảng 16

    private String buildKey(String customerId) {
        return CART_KEY_PREFIX + customerId;
    }

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        Object raw = redisTemplate.opsForValue().get(buildKey(customerId));
        if (raw instanceof Cart) {
            return Optional.of((Cart) raw);
        }
        return Optional.empty();
    }

    @Override
    public void save(Cart cart) {
        String key = buildKey(cart.getCustomerId());
        redisTemplate.opsForValue().set(key, cart, CART_TTL);
    }

    @Override
    public void deleteByCustomerId(String customerId) {
        redisTemplate.delete(buildKey(customerId));
    }
}

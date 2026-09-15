package com.ecommerce.order.infrastructure.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service executing atomic Redis Lua scripts for stock reservation and user purchase limit validation.
 * Corresponds to Đoạn mã 1 in Graduation Project report.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLuaService {

    private final StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> reserveScript;
    private DefaultRedisScript<Long> releaseScript;

    @PostConstruct
    public void init() {
        reserveScript = new DefaultRedisScript<>();
        reserveScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/reserve_stock.lua")));
        reserveScript.setResultType(Long.class);

        releaseScript = new DefaultRedisScript<>();
        releaseScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/release_stock.lua")));
        releaseScript.setResultType(Long.class);
    }

    /**
     * Executes atomic Lua script to check available stock and user purchase limit,
     * decrementing stock and locking user key with TTL in a single atomic operation.
     *
     * @param saleId flash sale ID
     * @param itemId item/product ID
     * @param customerId user/customer Keycloak sub
     * @param quantity purchase quantity requested
     * @param userLockTtlSeconds TTL for the user reservation lock (e.g. 300s)
     * @return true if reservation succeeded (script returned 1), false otherwise
     */
    public boolean reserveStock(Long saleId, Long itemId, String customerId, int quantity, int userLockTtlSeconds) {
        String stockKey = String.format("flashsale:%d:item:%d:stock", saleId, itemId);
        String userKey = String.format("flashsale:%d:item:%d:user:%s", saleId, itemId, customerId);

        List<String> keys = List.of(stockKey, userKey);
        try {
            Long result = stringRedisTemplate.execute(
                    reserveScript,
                    keys,
                    String.valueOf(quantity),
                    "1", // Default userLimit = 1 per flash sale
                    String.valueOf(userLockTtlSeconds)
            );

            log.info("Redis reserveStock script result: {} for saleId: {}, itemId: {}, customerId: {}",
                    result, saleId, itemId, customerId);
            return result != null && result == 1L;
        } catch (Exception ex) {
            log.error("Error executing Redis reserveStock script: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Compensating transaction: restores reserved stock and clears user key on Redis.
     */
    public boolean releaseReservation(Long saleId, Long itemId, String customerId, int quantity) {
        String stockKey = String.format("flashsale:%d:item:%d:stock", saleId, itemId);
        String userKey = String.format("flashsale:%d:item:%d:user:%s", saleId, itemId, customerId);

        List<String> keys = List.of(stockKey, userKey);
        try {
            Long result = stringRedisTemplate.execute(
                    releaseScript,
                    keys,
                    String.valueOf(quantity)
            );

            log.info("Redis releaseReservation script result: {} for saleId: {}, itemId: {}, customerId: {}",
                    result, saleId, itemId, customerId);
            return result != null && result == 1L;
        } catch (Exception ex) {
            log.error("Error executing Redis releaseReservation script: {}", ex.getMessage(), ex);
            return false;
        }
    }
}

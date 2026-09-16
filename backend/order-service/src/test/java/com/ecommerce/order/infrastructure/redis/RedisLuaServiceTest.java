package com.ecommerce.order.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisLuaServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private RedisLuaService redisLuaService;

    @BeforeEach
    void setUp() {
        redisLuaService = new RedisLuaService(stringRedisTemplate);
        redisLuaService.init();
    }

    @Test
    void testReserveStockSuccess() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenReturn(1L);

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertTrue(result);
    }

    @Test
    void testReserveStockOutOfStock() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenReturn(-1L);

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertFalse(result);
    }

    @Test
    void testReserveStockUserLimitExceeded() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenReturn(-2L);

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertFalse(result);
    }

    @Test
    void testReserveStockException() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenThrow(new RuntimeException("Redis connection error"));

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertFalse(result);
    }

    @Test
    void testReleaseReservationSuccess() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any()))
                .thenReturn(1L);

        boolean result = redisLuaService.releaseReservation(1L, 101L, "user_123", 1);
        assertTrue(result);
    }

    @Test
    void testReleaseReservationException() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any()))
                .thenThrow(new RuntimeException("Redis connection error"));

        boolean result = redisLuaService.releaseReservation(1L, 101L, "user_123", 1);
        assertFalse(result);
    }

    @Test
    void testReserveStockNullResult() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenReturn(null);

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertFalse(result);
    }

    @Test
    void testReserveStockNonOneResult() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(), any(), any()))
                .thenReturn(0L);

        boolean result = redisLuaService.reserveStock(1L, 101L, "user_123", 1, 300);
        assertFalse(result);
    }

    @Test
    void testReleaseReservationNullResult() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any()))
                .thenReturn(null);

        boolean result = redisLuaService.releaseReservation(1L, 101L, "user_123", 1);
        assertFalse(result);
    }

    @Test
    void testReleaseReservationNonOneResult() {
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any()))
                .thenReturn(0L);

        boolean result = redisLuaService.releaseReservation(1L, 101L, "user_123", 1);
        assertFalse(result);
    }
}

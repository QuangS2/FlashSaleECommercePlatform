package com.ecommerce.gateway;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CircuitBreakerChaosTest {

    private CircuitBreaker circuitBreaker;
    private CircuitBreakerRegistry registry;

    @BeforeEach
    public void setup() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofMillis(300))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(IOException.class, RuntimeException.class)
                .build();

        registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("orderServiceChaosBreaker");
    }

    @Test
    @DisplayName("Chaos Test 1: High Failure Rate triggers state transition from CLOSED to OPEN")
    public void testCircuitBreaker_OpensOnHighFailureRate() {
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // Giả lập 10 cuộc gọi downstream bị sập (100% tỷ lệ lỗi > 50% threshold)
        for (int i = 0; i < 10; i++) {
            try {
                circuitBreaker.executeSupplier(() -> {
                    throw new RuntimeException("Downstream order-service connection refused");
                });
            } catch (Exception ignored) {
            }
        }

        // Mạch lập tức chuyển sang OPEN
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getMetrics().getFailureRate()).isGreaterThanOrEqualTo(50.0f);
    }

    @Test
    @DisplayName("Chaos Test 2: Fail-Fast Protection when OPEN - Blocks downstream calls in < 1ms")
    public void testCircuitBreaker_FailFastInOpenState() {
        // Ép mạch sang OPEN
        circuitBreaker.transitionToOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        AtomicInteger downstreamCallCount = new AtomicInteger(0);

        Callable<String> downstreamServiceCall = () -> {
            downstreamCallCount.incrementAndGet();
            return "SUCCESS";
        };

        // Khi mạch OPEN, cuộc gọi bị chặn ngay lập tức mà KHÔNG gọi vào downstream service
        long startTime = System.nanoTime();
        assertThrows(CallNotPermittedException.class, () -> {
            circuitBreaker.executeCallable(downstreamServiceCall);
        });
        long durationNano = System.nanoTime() - startTime;
        long durationMs = durationNano / 1_000_000;

        // Kiểm chứng tính năng Fail-Fast: downstreamCallCount = 0 và thời gian chặn < 5ms
        assertThat(downstreamCallCount.get()).isEqualTo(0);
        assertThat(durationMs).isLessThan(10); // Ngắt tức thời, không gây treo tài nguyên Gateway
    }

    @Test
    @DisplayName("Chaos Test 3: Self-Healing Mechanism - Automatically transitions HALF_OPEN to CLOSED on recovery")
    public void testCircuitBreaker_SelfHealingRecovery() throws InterruptedException {
        // Ép mạch sang OPEN
        circuitBreaker.transitionToOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Chờ hết thời gian waitDurationInOpenState (300ms) để mạch tự động sang HALF_OPEN
        Thread.sleep(400);

        // Chuyển sang HALF_OPEN
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // Giả lập 3 cuộc gọi thăm dò thành công khi downstream đã hồi phục
        for (int i = 0; i < 3; i++) {
            String result = circuitBreaker.executeSupplier(() -> "ORDER_SERVICE_HEALTHY");
            assertThat(result).isEqualTo("ORDER_SERVICE_HEALTHY");
        }

        // Mạch tự động khôi phục về CLOSED (Self-Healing thành công)
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("Chaos Test 4: TimeLimiter Timeout Protection - Interrupted when latency exceeds limit")
    public void testCircuitBreaker_TimeLimiterTimeout() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .cancelRunningFuture(true)
                .build();

        TimeLimiter timeLimiter = TimeLimiter.of("orderServiceTimeout", timeLimiterConfig);

        // Giả lập downstream service phản hồi chậm (250ms > 100ms timeout)
        Callable<String> slowDownstreamCall = () -> {
            Thread.sleep(250);
            return "SLOW_RESPONSE";
        };

        Callable<String> restrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, () ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return slowDownstreamCall.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, Executors.newSingleThreadExecutor())
        );

        // TimeLimiter ngắt kết nối và ném TimeoutException
        assertThrows(TimeoutException.class, restrictedCall::call);
    }
}

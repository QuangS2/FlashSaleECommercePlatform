package com.ecommerce.inventory.infrastructure.lock.adapter;

import com.ecommerce.inventory.domain.port.out.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockAdapter implements DistributedLockPort {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Callable<T> task) throws Exception {
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
        if (!isLocked) {
            return null; // Lock could not be acquired
        }

        try {
            return task.call();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

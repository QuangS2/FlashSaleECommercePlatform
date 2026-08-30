package com.ecommerce.inventory.domain.port.out;

import java.util.concurrent.Callable;

/**
 * Outbound Port for Distributed Lock.
 * Abstracts the locking mechanism (e.g., Redisson) away from the Domain and Application layers.
 */
public interface DistributedLockPort {

    /**
     * Executes the given task under a distributed lock.
     *
     * @param lockKey The key to lock on (e.g., product ID)
     * @param waitTimeSeconds Maximum time to wait for the lock
     * @param leaseTimeSeconds Maximum time to hold the lock
     * @param task The task to execute while holding the lock
     * @param <T> The return type of the task
     * @return The result of the task, or null if lock could not be acquired
     * @throws Exception if the task throws an exception or thread is interrupted
     */
    <T> T executeWithLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Callable<T> task) throws Exception;
}

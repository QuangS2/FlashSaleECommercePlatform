package com.ecommerce.inventory.infrastructure.lock.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedissonLockAdapterTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private RedissonLockAdapter redissonLockAdapter;

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
    }

    @Test
    void testExecuteWithLock_Success() throws Exception {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        
        Callable<String> task = () -> "Success";
        
        String result = redissonLockAdapter.executeWithLock("myLock", 5, 5, task);
        
        assertEquals("Success", result);
        verify(rLock, times(1)).unlock();
    }

    @Test
    void testExecuteWithLock_FailedToAcquire() throws Exception {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        
        Callable<String> task = () -> "Success";
        
        String result = redissonLockAdapter.executeWithLock("myLock", 5, 5, task);
        
        assertNull(result);
        verify(rLock, never()).unlock();
    }

    @Test
    void testExecuteWithLock_Exception() throws Exception {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("Interrupted"));
        
        Callable<String> task = () -> "Success";
        
        assertThrows(Exception.class, () -> redissonLockAdapter.executeWithLock("myLock", 5, 5, task));
    }
}

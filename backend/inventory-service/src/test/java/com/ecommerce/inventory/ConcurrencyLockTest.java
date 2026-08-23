package com.ecommerce.inventory;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Disabled("Integration test requiring live Redis and MySQL containers")
public class ConcurrencyLockTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    private static final String TEST_PRODUCT_ID = "FLASH-SALE-PROD-001";
    private static final int INITIAL_STOCK = 100;
    private static final int THREAD_COUNT = 200;

    @BeforeEach
    public void setup() {
        // Xóa sạch kho và khởi tạo lại dữ liệu
        inventoryRepository.deleteAll();
        Inventory inv = new Inventory();
        inv.setProductId(TEST_PRODUCT_ID);
        inv.setQuantity(INITIAL_STOCK);
        inventoryRepository.save(inv);
    }

    @Test
    public void testRedissonLockPreventsOverselling() throws InterruptedException {
        // Dùng 200 threads để mô phỏng 200 người dùng bấm "Mua Ngay" cùng lúc
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        
        // Cản 200 threads chờ nhau ở vạch xuất phát
        CountDownLatch startLatch = new CountDownLatch(1);
        
        // Đợi 200 threads chạy xong
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    // Chờ tín hiệu thả vạch xuất phát
                    startLatch.await();
                    
                    boolean success = inventoryService.deductInventory(TEST_PRODUCT_ID, 1);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    // Đánh dấu đã chạy xong
                    endLatch.countDown();
                }
            });
        }

        System.out.println("====== BẮT ĐẦU TEST MULTI-THREADING (REDISSON LOCK) ======");
        // Bắn tín hiệu cho cả 200 threads đồng loạt chạy
        startLatch.countDown();
        
        // Đợi cho đến khi tất cả threads hoàn thành
        endLatch.await();
        
        System.out.println("====== KẾT THÚC TEST ======");
        System.out.println("Số lượng mua thành công: " + successCount.get());
        System.out.println("Số lượng mua thất bại (Hết hàng hoặc Timeout): " + failCount.get());

        // Lấy thông tin kho cuối cùng
        Inventory finalInventory = inventoryRepository.findByProductId(TEST_PRODUCT_ID).orElseThrow();
        System.out.println("Tồn kho còn lại trong MySQL: " + finalInventory.getQuantity());

        // Kì vọng: 100 người mua thành công, 100 người thất bại
        assertEquals(INITIAL_STOCK, successCount.get());
        assertEquals(THREAD_COUNT - INITIAL_STOCK, failCount.get());
        
        // Kì vọng: Tồn kho không bao giờ bị âm
        assertEquals(0, finalInventory.getQuantity());
    }
}

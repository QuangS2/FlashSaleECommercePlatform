package com.ecommerce.inventory.infrastructure.seeder;

import com.ecommerce.inventory.domain.entity.Inventory;
import com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDataSeeder implements CommandLineRunner {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public void run(String... args) {
        if (inventoryRepositoryPort.findByProductId("fs-101").isPresent()) {
            log.info("[InventoryDataSeeder] MySQL đã có dữ liệu tồn kho khởi tạo, bỏ qua.");
            return;
        }

        log.info("[InventoryDataSeeder] Bắt đầu tự động khởi tạo số lượng tồn kho vào MySQL...");

        Map<String, Integer> seedStocks = Map.of(
                "fs-101", 15,
                "fs-102", 8,
                "fs-103", 5,
                "fs-104", 32,
                "cat-1", 45,
                "cat-2", 28,
                "cat-3", 60,
                "cat-4", 19,
                "cat-5", 22,
                "cat-6", 12
        );

        seedStocks.forEach((productId, stock) -> {
            Inventory inventory = Inventory.builder()
                    .productId(productId)
                    .quantity(stock)
                    .reservedQuantity(0)
                    .build();
            inventoryRepositoryPort.save(inventory);
        });

        log.info("[InventoryDataSeeder] Đã nạp thành công tồn kho cho {} sản phẩm vào MySQL!", seedStocks.size());
    }
}

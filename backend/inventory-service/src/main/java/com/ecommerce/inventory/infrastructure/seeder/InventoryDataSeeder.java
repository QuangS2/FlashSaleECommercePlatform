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
        log.info("[InventoryDataSeeder] Bắt đầu đồng bộ số lượng tồn kho cho toàn bộ 24 sản phẩm vào MySQL...");

        Map<String, Integer> seedStocks = Map.ofEntries(
                // Flash sale products
                Map.entry("fs-101", 15),
                Map.entry("fs-102", 8),
                Map.entry("fs-103", 5),
                Map.entry("fs-104", 32),

                // Catalog products
                Map.entry("cat-1", 45),
                Map.entry("cat-2", 28),
                Map.entry("cat-3", 60),
                Map.entry("cat-4", 19),
                Map.entry("cat-5", 22),
                Map.entry("cat-6", 12),
                Map.entry("cat-7", 35),
                Map.entry("cat-8", 16),
                Map.entry("cat-9", 50),
                Map.entry("cat-10", 18),
                Map.entry("cat-11", 20),
                Map.entry("cat-12", 40),
                Map.entry("cat-13", 25),
                Map.entry("cat-14", 14),
                Map.entry("cat-15", 28),
                Map.entry("cat-16", 15),
                Map.entry("cat-17", 10),
                Map.entry("cat-18", 45),
                Map.entry("cat-19", 30),
                Map.entry("cat-20", 25)
        );

        seedStocks.forEach((productId, stock) -> {
            if (inventoryRepositoryPort.findByProductId(productId).isEmpty()) {
                Inventory inventory = Inventory.builder()
                        .productId(productId)
                        .quantity(stock)
                        .reservedQuantity(0)
                        .build();
                inventoryRepositoryPort.save(inventory);
            }
        });

        log.info("[InventoryDataSeeder] Đã đồng bộ thành công tồn kho cho {} sản phẩm vào MySQL!", seedStocks.size());
    }
}

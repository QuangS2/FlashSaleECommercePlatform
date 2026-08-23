package com.ecommerce.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
@EnableScheduling
@ConditionalOnProperty(name = "mock.simulator.enabled", havingValue = "true", matchIfMissing = false)
public class MockStockSimulator {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();
    private final String[] flashSaleProductIds = {"fs-101", "fs-102", "fs-103", "fs-104"};
    // matching initial mock data from frontend
    private final int[] currentStocks = {15, 8, 5, 32};

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    public void simulateStockUpdate() {
        int index = random.nextInt(flashSaleProductIds.length);
        if (currentStocks[index] > 0) {
            // Decrease stock by a random amount (1-2)
            int decrement = random.nextInt(2) + 1;
            currentStocks[index] = Math.max(0, currentStocks[index] - decrement);

            String productId = flashSaleProductIds[index];
            int remaining = currentStocks[index];

            // Payload format matching JSON
            Map<String, Object> payload = Map.of(
                    "productId", productId,
                    "remainingStock", remaining,
                    "timestamp", System.currentTimeMillis()
            );

            // Broadcast to all clients subscribed to /topic/flashsale-stock
            messagingTemplate.convertAndSend("/topic/flashsale-stock", payload);

            System.out.println("Mock Kafka Event: Decreased stock for " + productId + " to " + remaining);
        }
    }
}

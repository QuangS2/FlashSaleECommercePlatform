package com.ecommerce.inventory.infrastructure.inbound.rest;

import com.ecommerce.inventory.dto.CreateFlashSaleRequest;
import com.ecommerce.inventory.dto.WarmupCacheRequest;
import com.ecommerce.inventory.infrastructure.persistence.entity.FlashSaleItemEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataFlashSaleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInventoryControllerTest {

    @Mock
    private SpringDataFlashSaleItemRepository flashSaleItemRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AdminInventoryController adminInventoryController;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateFlashSaleCampaignSuccess() {
        CreateFlashSaleRequest.FlashSaleItemDto itemDto = CreateFlashSaleRequest.FlashSaleItemDto.builder()
                .productId("PROD-100")
                .originalPrice(new BigDecimal("100.00"))
                .flashPrice(new BigDecimal("69.00"))
                .allocatedStock(50)
                .userLimit(1)
                .build();

        CreateFlashSaleRequest request = CreateFlashSaleRequest.builder()
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .items(List.of(itemDto))
                .build();

        when(flashSaleItemRepository.save(any(FlashSaleItemEntity.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Map<String, Object>> response = adminInventoryController.createFlashSaleCampaign(request);

        assertNotNull(response.getBody());
        assertEquals(201, response.getStatusCode().value());
        assertEquals(1, response.getBody().get("itemsCount"));
        verify(flashSaleItemRepository, times(1)).save(any());
    }

    @Test
    void testCreateFlashSaleCampaignWithNullItems() {
        CreateFlashSaleRequest request = CreateFlashSaleRequest.builder()
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .items(null)
                .build();

        ResponseEntity<Map<String, Object>> response = adminInventoryController.createFlashSaleCampaign(request);

        assertNotNull(response.getBody());
        assertEquals(201, response.getStatusCode().value());
        assertEquals(0, response.getBody().get("itemsCount"));
        verify(flashSaleItemRepository, never()).save(any());
    }

    @Test
    void testWarmupInventoryCacheSuccess() {
        WarmupCacheRequest request = new WarmupCacheRequest(100L);
        FlashSaleItemEntity item = FlashSaleItemEntity.builder()
                .id(1L)
                .flashSaleId(100L)
                .productId("PROD-100")
                .availableStock(50)
                .build();

        when(flashSaleItemRepository.findByFlashSaleId(100L)).thenReturn(List.of(item));

        ResponseEntity<Map<String, Object>> response = adminInventoryController.warmupInventoryCache(request);

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().get("itemsWarmedUp"));
        verify(valueOperations, times(1)).set(eq("flashsale:100:item:1:stock"), eq("50"), any(Duration.class));
    }

    @Test
    void testReconcileInventory() {
        FlashSaleItemEntity validItem = FlashSaleItemEntity.builder()
                .id(1L)
                .allocatedStock(100)
                .availableStock(80)
                .reservedStock(10)
                .soldStock(10)
                .build();

        FlashSaleItemEntity invalidItem = FlashSaleItemEntity.builder()
                .id(2L)
                .allocatedStock(100)
                .availableStock(80)
                .reservedStock(10)
                .soldStock(5) // sum = 95 != 100
                .build();

        when(flashSaleItemRepository.findAll()).thenReturn(List.of(validItem, invalidItem));

        ResponseEntity<Map<String, Object>> response = adminInventoryController.reconcileInventory();

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("totalItemsChecked"));
        assertEquals(1, response.getBody().get("invariantPassed"));
        assertEquals(1, response.getBody().get("reconciledCount"));
    }

    @Test
    void testResetDemoDataSuccess() {
        FlashSaleItemEntity item = FlashSaleItemEntity.builder()
                .id(1L)
                .allocatedStock(50)
                .availableStock(10)
                .reservedStock(20)
                .soldStock(20)
                .build();
        when(flashSaleItemRepository.findAll()).thenReturn(List.of(item));
        when(inventoryRepositoryPort.findByProductId(anyString())).thenReturn(java.util.Optional.empty());

        ResponseEntity<Map<String, Object>> response = adminInventoryController.resetDemoData();

        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals(24, response.getBody().get("productsReset"));
        assertEquals(1, response.getBody().get("flashSaleItemsReset"));
        verify(flashSaleItemRepository, atLeastOnce()).save(any(FlashSaleItemEntity.class));
        verify(inventoryRepositoryPort, times(24)).save(any());
    }
}

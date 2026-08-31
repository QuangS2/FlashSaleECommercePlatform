package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.inventory.domain.entity.Inventory;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryJpaAdapterTest {

    @Mock
    private SpringDataInventoryRepository jpaRepository;

    @InjectMocks
    private InventoryJpaAdapter inventoryJpaAdapter;

    private InventoryEntity mockEntity;
    private Inventory mockDomain;

    @BeforeEach
    void setUp() {
        mockEntity = InventoryEntity.builder()
                .id(1L)
                .productId("prod_1")
                .quantity(100)
                .reservedQuantity(10)
                .build();

        mockDomain = Inventory.builder()
                .id(1L)
                .productId("prod_1")
                .quantity(100)
                .reservedQuantity(10)
                .build();
    }

    @Test
    void testFindByProductId_Found() {
        when(jpaRepository.findByProductId("prod_1")).thenReturn(Optional.of(mockEntity));

        Optional<Inventory> result = inventoryJpaAdapter.findByProductId("prod_1");

        assertTrue(result.isPresent());
        assertEquals("prod_1", result.get().getProductId());
        assertEquals(100, result.get().getQuantity());
        assertEquals(10, result.get().getReservedQuantity());

        verify(jpaRepository, times(1)).findByProductId("prod_1");
    }

    @Test
    void testFindByProductId_NotFound() {
        when(jpaRepository.findByProductId("prod_1")).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryJpaAdapter.findByProductId("prod_1");

        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findByProductId("prod_1");
    }

    @Test
    void testSave() {
        when(jpaRepository.save(any(InventoryEntity.class))).thenReturn(mockEntity);

        Inventory result = inventoryJpaAdapter.save(mockDomain);

        assertNotNull(result);
        assertEquals("prod_1", result.getProductId());
        assertEquals(100, result.getQuantity());
        assertEquals(10, result.getReservedQuantity());

        verify(jpaRepository, times(1)).save(any(InventoryEntity.class));
    }
}

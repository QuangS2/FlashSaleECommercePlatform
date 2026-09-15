package com.ecommerce.inventory.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FlashSaleItemTest {

    @Test
    void testValidateInvariant_Success() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(100)
                .availableStock(70)
                .reservedStock(20)
                .soldStock(10)
                .build();

        assertTrue(item.validateInvariant());
    }

    @Test
    void testValidateInvariant_Failure() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(100)
                .availableStock(70)
                .reservedStock(20)
                .soldStock(20) // 70 + 20 + 20 = 110 != 100
                .build();

        assertFalse(item.validateInvariant());
    }

    @Test
    void testReserve_Success() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(50)
                .availableStock(50)
                .reservedStock(0)
                .soldStock(0)
                .build();

        boolean result = item.reserve(5);
        assertTrue(result);
        assertEquals(45, item.getAvailableStock());
        assertEquals(5, item.getReservedStock());
        assertTrue(item.validateInvariant());
    }

    @Test
    void testReserve_OutOfStock() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(10)
                .availableStock(2)
                .reservedStock(8)
                .soldStock(0)
                .build();

        boolean result = item.reserve(5);
        assertFalse(result);
        assertEquals(2, item.getAvailableStock());
    }

    @Test
    void testReserve_InvalidAmount() {
        FlashSaleItem item = FlashSaleItem.builder().availableStock(10).build();
        assertThrows(IllegalArgumentException.class, () -> item.reserve(0));
    }

    @Test
    void testConfirmSold_Success() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(10)
                .availableStock(5)
                .reservedStock(5)
                .soldStock(0)
                .build();

        boolean result = item.confirmSold(3);
        assertTrue(result);
        assertEquals(2, item.getReservedStock());
        assertEquals(3, item.getSoldStock());
        assertTrue(item.validateInvariant());
    }

    @Test
    void testConfirmSold_InsufficientReserved() {
        FlashSaleItem item = FlashSaleItem.builder()
                .reservedStock(2)
                .soldStock(0)
                .build();

        boolean result = item.confirmSold(5);
        assertFalse(result);
    }

    @Test
    void testConfirmSold_InvalidAmount() {
        FlashSaleItem item = FlashSaleItem.builder().reservedStock(5).build();
        assertThrows(IllegalArgumentException.class, () -> item.confirmSold(-1));
    }

    @Test
    void testReleaseReservation() {
        FlashSaleItem item = FlashSaleItem.builder()
                .allocatedStock(10)
                .availableStock(5)
                .reservedStock(5)
                .soldStock(0)
                .build();

        item.releaseReservation(3);
        assertEquals(8, item.getAvailableStock());
        assertEquals(2, item.getReservedStock());
        assertTrue(item.validateInvariant());
    }

    @Test
    void testReleaseReservation_InvalidAmount() {
        FlashSaleItem item = FlashSaleItem.builder().reservedStock(5).build();
        assertThrows(IllegalArgumentException.class, () -> item.releaseReservation(0));
    }
}

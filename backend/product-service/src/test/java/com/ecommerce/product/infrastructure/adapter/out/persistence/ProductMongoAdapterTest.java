package com.ecommerce.product.infrastructure.adapter.out.persistence;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.ecommerce.product.infrastructure.adapter.out.persistence.repository.MongoProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductMongoAdapterTest {

    @Mock
    private MongoProductRepository mongoRepository;

    @InjectMocks
    private ProductMongoAdapter productMongoAdapter;

    private ProductEntity mockEntity;
    private Product mockDomain;

    @BeforeEach
    void setUp() {
        mockEntity = ProductEntity.builder()
                .id("prod_1")
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1500.00"))
                .discountPrice(new BigDecimal("1400.00"))
                .imageUrl("http://example.com/laptop.jpg")
                .build();

        mockDomain = Product.builder()
                .id("prod_1")
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1500.00"))
                .discountPrice(new BigDecimal("1400.00"))
                .imageUrl("http://example.com/laptop.jpg")
                .build();
    }

    @Test
    void testFindAll() {
        when(mongoRepository.findAll()).thenReturn(List.of(mockEntity));

        List<Product> result = productMongoAdapter.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prod_1", result.get(0).getId());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Gaming Laptop", result.get(0).getDescription());
        assertEquals(new BigDecimal("1500.00"), result.get(0).getPrice());
        assertEquals(new BigDecimal("1400.00"), result.get(0).getDiscountPrice());
        assertEquals("http://example.com/laptop.jpg", result.get(0).getImageUrl());

        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        when(mongoRepository.findById("prod_1")).thenReturn(Optional.of(mockEntity));

        Optional<Product> result = productMongoAdapter.findById("prod_1");

        assertTrue(result.isPresent());
        assertEquals("prod_1", result.get().getId());
        assertEquals("Laptop", result.get().getName());
        
        verify(mongoRepository, times(1)).findById("prod_1");
    }

    @Test
    void testFindById_NotFound() {
        when(mongoRepository.findById("prod_2")).thenReturn(Optional.empty());

        Optional<Product> result = productMongoAdapter.findById("prod_2");

        assertFalse(result.isPresent());
        verify(mongoRepository, times(1)).findById("prod_2");
    }

    @Test
    void testSave() {
        when(mongoRepository.save(any(ProductEntity.class))).thenReturn(mockEntity);

        Product result = productMongoAdapter.save(mockDomain);

        assertNotNull(result);
        assertEquals("prod_1", result.getId());
        assertEquals("Laptop", result.getName());

        verify(mongoRepository, times(1)).save(any(ProductEntity.class));
    }
}

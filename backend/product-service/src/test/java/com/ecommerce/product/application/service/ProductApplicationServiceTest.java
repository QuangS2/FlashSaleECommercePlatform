package com.ecommerce.product.application.service;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductApplicationServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private ProductApplicationService productApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        Product p1 = Product.builder().id("1").name("Product 1").build();
        Product p2 = Product.builder().id("2").name("Product 2").build();
        when(productRepositoryPort.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> products = productApplicationService.getAllProducts();

        assertEquals(2, products.size());
        verify(productRepositoryPort, times(1)).findAll();
    }

    @Test
    void testGetProductById_Found() {
        Product p1 = Product.builder().id("1").name("Product 1").build();
        when(productRepositoryPort.findById("1")).thenReturn(Optional.of(p1));

        Optional<Product> product = productApplicationService.getProductById("1");

        assertTrue(product.isPresent());
        assertEquals("1", product.get().getId());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepositoryPort.findById("99")).thenReturn(Optional.empty());

        Optional<Product> product = productApplicationService.getProductById("99");

        assertFalse(product.isPresent());
    }

    @Test
    void testCreateProduct() {
        Product p1 = Product.builder().name("Product 1").price(BigDecimal.valueOf(100)).build();
        Product savedP1 = Product.builder().id("1").name("Product 1").price(BigDecimal.valueOf(100)).build();
        when(productRepositoryPort.save(any(Product.class))).thenReturn(savedP1);

        Product result = productApplicationService.createProduct(p1);

        assertNotNull(result.getId());
        assertEquals("Product 1", result.getName());
        verify(productRepositoryPort, times(1)).save(p1);
    }
}

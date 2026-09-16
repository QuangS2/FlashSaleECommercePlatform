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

    @Mock
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Mock
    private org.springframework.data.redis.core.ValueOperations<String, String> valueOperations;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private ProductApplicationService productApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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

    @Test
    void testGetProductById_CacheHit() throws Exception {
        Product p1 = Product.builder().id("10").name("Product 10").build();
        when(valueOperations.get("product:detail:10")).thenReturn("{\"id\":\"10\",\"name\":\"Product 10\"}");
        when(objectMapper.readValue(anyString(), eq(Product.class))).thenReturn(p1);

        Optional<Product> product = productApplicationService.getProductById("10");

        assertTrue(product.isPresent());
        assertEquals("10", product.get().getId());
        verify(productRepositoryPort, never()).findById(anyString());
    }

    @Test
    void testIncrementSoldCount() {
        Product p = Product.builder().id("1").name("Product 1").soldCount(5).build();
        when(productRepositoryPort.findById("1")).thenReturn(Optional.of(p));
        when(productRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        Product updated = productApplicationService.incrementSoldCount("1", 3);

        assertEquals(8, updated.getSoldCount());
    }

    @Test
    void testGetProductById_CacheException_FallsBackToDb() {
        Product p = Product.builder().id("10").name("Product 10").build();
        when(valueOperations.get("product:detail:10")).thenThrow(new RuntimeException("Redis down"));
        when(productRepositoryPort.findById("10")).thenReturn(Optional.of(p));

        Optional<Product> product = productApplicationService.getProductById("10");

        assertTrue(product.isPresent());
        assertEquals("10", product.get().getId());
    }

    @Test
    void testGetProductById_CacheWriteException_Ignored() throws Exception {
        Product p = Product.builder().id("20").name("Product 20").build();
        when(valueOperations.get("product:detail:20")).thenReturn(null);
        when(productRepositoryPort.findById("20")).thenReturn(Optional.of(p));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failure"));

        Optional<Product> product = productApplicationService.getProductById("20");

        assertTrue(product.isPresent());
        assertEquals("20", product.get().getId());
    }
}

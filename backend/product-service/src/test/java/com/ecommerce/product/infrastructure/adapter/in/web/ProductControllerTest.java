package com.ecommerce.product.infrastructure.adapter.in.web;

import com.ecommerce.product.application.dto.CreateProductRequest;
import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.in.ProductUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductUseCase productUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllProducts() throws Exception {
        Product p1 = Product.builder().id("1").name("Product 1").category("Laptop").price(BigDecimal.valueOf(100)).build();
        Product p2 = Product.builder().id("2").name("Product 2").category("Phone").price(BigDecimal.valueOf(200)).build();
        
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    void testGetAllProducts_WithCategoryAll() throws Exception {
        Product p1 = Product.builder().id("1").name("Product 1").category("Laptop").price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("category", "Tất cả"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testGetAllProducts_WithCategoryFilter_Mismatch() throws Exception {
        Product p1 = Product.builder().id("1").name("Product 1").category("Laptop").price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("category", "Phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetAllProducts_WithSearchMatchingCategory() throws Exception {
        Product p1 = Product.builder().id("1").name("Device A").category("Accessories").price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("search", "Accessories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testGetAllProducts_WithSearchEmpty() throws Exception {
        Product p1 = Product.builder().id("1").name("Device A").category("Accessories").price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("search", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testGetAllProducts_WithSearchMismatch() throws Exception {
        Product p1 = Product.builder().id("1").name("Device A").category("Accessories").price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("search", "NonExisting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetAllProducts_WithNullNameAndCategory() throws Exception {
        Product p1 = Product.builder().id("1").name(null).category(null).price(BigDecimal.valueOf(100)).build();
        when(productUseCase.getAllProducts()).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/v1/products").param("search", "query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetProductById_Found() throws Exception {
        Product p1 = Product.builder().id("1").name("Product 1").price(BigDecimal.valueOf(100)).build();
        
        when(productUseCase.getProductById("1")).thenReturn(Optional.of(p1));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productUseCase.getProductById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProduct() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Product 1")
                .description("Desc")
                .price(BigDecimal.valueOf(100))
                .build();

        Product savedProduct = Product.builder()
                .id("1")
                .name("Product 1")
                .description("Desc")
                .price(BigDecimal.valueOf(100))
                .build();

        when(productUseCase.createProduct(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Product 1"));
    }
}

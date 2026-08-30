package com.ecommerce.inventory.infrastructure.inbound.rest;

import com.ecommerce.inventory.application.port.in.InventoryUseCase;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryUseCase inventoryUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = InventoryResponse.builder()
                .productId("prod_1")
                .availableQuantity(10)
                .reservedQuantity(0)
                .message("Success")
                .build();
    }

    @Test
    void testGetInventory() throws Exception {
        when(inventoryUseCase.getInventoryByProductId("prod_1")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/inventory/{productId}", "prod_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("prod_1"))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    void testCheckStock() throws Exception {
        when(inventoryUseCase.isInStock("prod_1", 5)).thenReturn(true);

        mockMvc.perform(get("/api/v1/inventory/{productId}/check", "prod_1")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testUpdateStock() throws Exception {
        when(inventoryUseCase.updateStock(any(UpdateStockRequest.class))).thenReturn(mockResponse);

        UpdateStockRequest request = UpdateStockRequest.builder()
                .productId("prod_1")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/v1/inventory/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("prod_1"))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("inventory-service"));
    }
}

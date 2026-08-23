package com.ecommerce.inventory;

import com.ecommerce.inventory.controller.InventoryController;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @Test
    @DisplayName("Test 1: GET /api/v1/inventory/{productId} - Returns inventory details")
    public void testGetInventoryByProductId() throws Exception {
        String productId = "PROD-IPHONE-15";
        InventoryResponse response = InventoryResponse.builder()
                .productId(productId)
                .availableQuantity(50)
                .reservedQuantity(5)
                .message("Tra cứu tồn kho thành công")
                .build();

        when(inventoryService.getInventoryByProductId(productId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.availableQuantity").value(50))
                .andExpect(jsonPath("$.reservedQuantity").value(5));
    }

    @Test
    @DisplayName("Test 2: POST /api/v1/inventory/stock - Updates stock and returns 200 OK")
    public void testUpdateStock() throws Exception {
        UpdateStockRequest request = UpdateStockRequest.builder()
                .productId("PROD-IPHONE-15")
                .quantity(100)
                .build();

        InventoryResponse response = InventoryResponse.builder()
                .productId("PROD-IPHONE-15")
                .availableQuantity(100)
                .reservedQuantity(0)
                .message("Cập nhật tồn kho thành công")
                .build();

        when(inventoryService.updateStock(any(UpdateStockRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventory/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("PROD-IPHONE-15"))
                .andExpect(jsonPath("$.availableQuantity").value(100));
    }

    @Test
    @DisplayName("Test 3: GET /api/v1/inventory/{productId}/check - Checks if stock is available")
    public void testIsInStock() throws Exception {
        when(inventoryService.isInStock("PROD-1", 2)).thenReturn(true);

        mockMvc.perform(get("/api/v1/inventory/PROD-1/check?quantity=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Test 4: GET /api/v1/inventory/status - Returns Service Health Status UP")
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("inventory-service"));
    }
}

package com.ecommerce.cart;

import com.ecommerce.cart.application.dto.AddToCartRequest;
import com.ecommerce.cart.application.dto.CartResponse;
import com.ecommerce.cart.application.service.CartApplicationService;
import com.ecommerce.cart.domain.entity.Cart;
import com.ecommerce.cart.domain.entity.CartItem;
import com.ecommerce.cart.domain.port.out.CartRepositoryPort;
import com.ecommerce.cart.infrastructure.adapter.in.web.CartController;
import com.ecommerce.cart.infrastructure.adapter.out.redis.RedisCartAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CartApplicationService cartApplicationService;

    private CartController cartController;
    private RedisCartAdapter redisCartAdapter;

    @BeforeEach
    void setUp() {
        cartController = new CartController(cartApplicationService);
        redisCartAdapter = new RedisCartAdapter(redisTemplate);
    }

    // ==========================================
    // 1. DOMAIN TESTS: CartItem (Tests 1 - 4)
    // ==========================================

    @Test
    @DisplayName("Test 1: CartItem subtotal calculation with valid quantity and price")
    void testCartItemSubtotalValid() {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .quantity(3)
                .unitPrice(new BigDecimal("150.00"))
                .build();

        assertEquals(new BigDecimal("450.00"), item.getSubtotal());
    }

    @Test
    @DisplayName("Test 2: CartItem subtotal returns ZERO when unit price is null")
    void testCartItemSubtotalNullPrice() {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .quantity(3)
                .unitPrice(null)
                .build();

        assertEquals(BigDecimal.ZERO, item.getSubtotal());
    }

    @Test
    @DisplayName("Test 3: CartItem subtotal returns ZERO when quantity is null")
    void testCartItemSubtotalNullQuantity() {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .quantity(null)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        assertEquals(BigDecimal.ZERO, item.getSubtotal());
    }

    @Test
    @DisplayName("Test 4: CartItem builder and getter integrity")
    void testCartItemBuilder() {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .productTitle("Flash Sale iPhone")
                .imageUrl("https://example.com/ip.jpg")
                .quantity(1)
                .unitPrice(new BigDecimal("999.00"))
                .build();

        assertEquals("prod-1", item.getProductId());
        assertEquals("Flash Sale iPhone", item.getProductTitle());
        assertEquals("https://example.com/ip.jpg", item.getImageUrl());
        assertEquals(1, item.getQuantity());
        assertEquals(new BigDecimal("999.00"), item.getUnitPrice());
    }

    // ==========================================
    // 2. DOMAIN TESTS: Cart (Tests 5 - 10)
    // ==========================================

    @Test
    @DisplayName("Test 5: Cart initial total amount is ZERO for empty cart")
    void testEmptyCartTotalAmount() {
        Cart cart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertEquals(0, cart.getTotalQuantity());
    }

    @Test
    @DisplayName("Test 6: Cart adds new item correctly")
    void testCartAddNewItem() {
        Cart cart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().productId("p1").quantity(2).unitPrice(new BigDecimal("50.00")).build();

        cart.addItem(item);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(new BigDecimal("100.00"), cart.getTotalAmount());
    }

    @Test
    @DisplayName("Test 7: Cart accumulates quantity when adding existing item")
    void testCartAddExistingItemAccumulatesQuantity() {
        Cart cart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        CartItem item1 = CartItem.builder().productId("p1").quantity(2).unitPrice(new BigDecimal("50.00")).build();
        CartItem item2 = CartItem.builder().productId("p1").quantity(3).unitPrice(new BigDecimal("50.00")).build();

        cart.addItem(item1);
        cart.addItem(item2);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getTotalQuantity());
        assertEquals(new BigDecimal("250.00"), cart.getTotalAmount());
    }

    @Test
    @DisplayName("Test 8: Cart removes item by productId correctly")
    void testCartRemoveItem() {
        Cart cart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        CartItem item1 = CartItem.builder().productId("p1").quantity(2).unitPrice(new BigDecimal("50.00")).build();
        CartItem item2 = CartItem.builder().productId("p2").quantity(1).unitPrice(new BigDecimal("100.00")).build();

        cart.addItem(item1);
        cart.addItem(item2);
        cart.removeItem("p1");

        assertEquals(1, cart.getItems().size());
        assertEquals("p2", cart.getItems().get(0).getProductId());
        assertEquals(new BigDecimal("100.00"), cart.getTotalAmount());
    }

    @Test
    @DisplayName("Test 9: Cart clear removes all items")
    void testCartClear() {
        Cart cart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        cart.addItem(CartItem.builder().productId("p1").quantity(1).unitPrice(BigDecimal.TEN).build());
        cart.clear();

        assertEquals(0, cart.getItems().size());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
    }

    @Test
    @DisplayName("Test 10: Cart handles null items safely on addItem and clear")
    void testCartNullItemsSafety() {
        Cart cart = new Cart();
        cart.setCustomerId("cust-safe");
        cart.setItems(null);

        cart.addItem(CartItem.builder().productId("p1").quantity(1).unitPrice(BigDecimal.TEN).build());
        assertNotNull(cart.getItems());
        assertEquals(1, cart.getItems().size());
    }

    // ==========================================
    // 3. APPLICATION SERVICE TESTS (Tests 11 - 15)
    // ==========================================

    @Test
    @DisplayName("Test 11: ApplicationService returns existing cart for customer")
    void testAppServiceGetExistingCart() {
        Cart existing = Cart.builder().customerId("c1").items(new ArrayList<>()).build();
        when(cartRepositoryPort.findByCustomerId("c1")).thenReturn(Optional.of(existing));

        CartResponse response = cartApplicationService.getCart("c1");
        assertNotNull(response);
        assertEquals("c1", response.getCustomerId());
        verify(cartRepositoryPort, times(1)).findByCustomerId("c1");
    }

    @Test
    @DisplayName("Test 12: ApplicationService creates empty cart if not found in repository")
    void testAppServiceGetEmptyCart() {
        when(cartRepositoryPort.findByCustomerId("c2")).thenReturn(Optional.empty());

        CartResponse response = cartApplicationService.getCart("c2");
        assertNotNull(response);
        assertEquals("c2", response.getCustomerId());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Test 13: ApplicationService addToCart saves cart to repository")
    void testAppServiceAddToCart() {
        when(cartRepositoryPort.findByCustomerId("c1")).thenReturn(Optional.empty());

        AddToCartRequest req = AddToCartRequest.builder()
                .productId("p1")
                .productTitle("Item 1")
                .quantity(2)
                .unitPrice(new BigDecimal("49.99"))
                .build();

        CartResponse response = cartApplicationService.addToCart("c1", req);

        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalQuantity());
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Test 14: ApplicationService removeFromCart removes item and saves")
    void testAppServiceRemoveFromCart() {
        Cart cart = Cart.builder().customerId("c1").items(new ArrayList<>()).build();
        cart.addItem(CartItem.builder().productId("p1").quantity(1).unitPrice(BigDecimal.ONE).build());
        when(cartRepositoryPort.findByCustomerId("c1")).thenReturn(Optional.of(cart));

        CartResponse response = cartApplicationService.removeFromCart("c1", "p1");

        assertTrue(response.getItems().isEmpty());
        verify(cartRepositoryPort, times(1)).save(cart);
    }

    @Test
    @DisplayName("Test 15: ApplicationService clearCart invokes repository delete")
    void testAppServiceClearCart() {
        cartApplicationService.clearCart("c1");
        verify(cartRepositoryPort, times(1)).deleteByCustomerId("c1");
    }

    // ==========================================
    // 4. REST CONTROLLER TESTS (Tests 16 - 20)
    // ==========================================

    @Test
    @DisplayName("Test 16: Controller getCart uses X-User-Id header if present")
    void testControllerGetCartHeader() {
        when(cartRepositoryPort.findByCustomerId("user-jwt-123")).thenReturn(Optional.empty());

        ResponseEntity<CartResponse> res = cartController.getCart("user-jwt-123", null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("user-jwt-123", res.getBody().getCustomerId());
    }

    @Test
    @DisplayName("Test 17: Controller getCart falls back to query parameter")
    void testControllerGetCartQuery() {
        when(cartRepositoryPort.findByCustomerId("user-query-456")).thenReturn(Optional.empty());

        ResponseEntity<CartResponse> res = cartController.getCart(null, "user-query-456");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("user-query-456", res.getBody().getCustomerId());
    }

    @Test
    @DisplayName("Test 18: Controller getCart falls back to anonymous-guest")
    void testControllerGetCartAnonymous() {
        when(cartRepositoryPort.findByCustomerId("anonymous-guest")).thenReturn(Optional.empty());

        ResponseEntity<CartResponse> res = cartController.getCart(null, null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("anonymous-guest", res.getBody().getCustomerId());
    }

    @Test
    @DisplayName("Test 19: Controller addToCart returns 200 OK with updated CartResponse")
    void testControllerAddToCart() {
        when(cartRepositoryPort.findByCustomerId("cust-ctrl")).thenReturn(Optional.empty());

        AddToCartRequest req = AddToCartRequest.builder()
                .productId("p9")
                .productTitle("Headphones")
                .quantity(1)
                .unitPrice(new BigDecimal("199.00"))
                .build();

        ResponseEntity<CartResponse> res = cartController.addToCart("cust-ctrl", null, req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().getItems().size());
    }

    @Test
    @DisplayName("Test 20: Controller clearCart returns 204 No Content")
    void testControllerClearCart() {
        ResponseEntity<Void> res = cartController.clearCart("cust-ctrl", null);
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        verify(cartRepositoryPort, times(1)).deleteByCustomerId("cust-ctrl");
    }

    // ==========================================
    // 5. REDIS ADAPTER TESTS (Tests 21 - 24)
    // ==========================================

    @Test
    @DisplayName("Test 21: RedisCartAdapter save stores cart with 7 days TTL")
    void testRedisAdapterSave() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Cart cart = Cart.builder().customerId("user-redis-1").items(new ArrayList<>()).build();
        redisCartAdapter.save(cart);

        verify(valueOperations, times(1)).set(eq("cart:user-redis-1"), eq(cart), eq(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("Test 22: RedisCartAdapter findByCustomerId returns present when found")
    void testRedisAdapterFindHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Cart cart = Cart.builder().customerId("user-redis-1").items(new ArrayList<>()).build();
        when(valueOperations.get("cart:user-redis-1")).thenReturn(cart);

        Optional<Cart> result = redisCartAdapter.findByCustomerId("user-redis-1");

        assertTrue(result.isPresent());
        assertEquals("user-redis-1", result.get().getCustomerId());
    }

    @Test
    @DisplayName("Test 23: RedisCartAdapter findByCustomerId returns empty on cache miss")
    void testRedisAdapterFindMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:user-redis-none")).thenReturn(null);

        Optional<Cart> result = redisCartAdapter.findByCustomerId("user-redis-none");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 24: RedisCartAdapter deleteByCustomerId removes key from Redis")
    void testRedisAdapterDelete() {
        redisCartAdapter.deleteByCustomerId("user-redis-del");
        verify(redisTemplate, times(1)).delete("cart:user-redis-del");
    }
}

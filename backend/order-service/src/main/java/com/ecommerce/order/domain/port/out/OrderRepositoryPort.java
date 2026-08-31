package com.ecommerce.order.domain.port.out;

import com.ecommerce.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Port for Order Repository.
 * This interface defines how the domain expects to interact with persistence.
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

}

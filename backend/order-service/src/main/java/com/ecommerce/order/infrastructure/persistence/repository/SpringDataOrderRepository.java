package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderId(String orderId);

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<OrderEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    boolean existsByOrderId(String orderId);
}

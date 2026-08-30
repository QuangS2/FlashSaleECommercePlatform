package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.port.out.OrderRepositoryPort;
import com.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity savedEntity = springDataOrderRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return springDataOrderRepository.findByOrderId(orderId)
                .map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByUserIdOrderByCreatedAtDesc(String userId) {
        return springDataOrderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());
    }
}

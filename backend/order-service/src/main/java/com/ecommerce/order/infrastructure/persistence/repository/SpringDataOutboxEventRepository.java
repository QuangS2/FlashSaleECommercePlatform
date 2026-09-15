package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);

    List<OutboxEventEntity> findByStatusAndAggregateType(String status, String aggregateType);
}

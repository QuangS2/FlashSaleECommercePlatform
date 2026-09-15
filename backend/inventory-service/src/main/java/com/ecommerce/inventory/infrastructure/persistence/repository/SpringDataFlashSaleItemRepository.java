package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.FlashSaleItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataFlashSaleItemRepository extends JpaRepository<FlashSaleItemEntity, Long> {

    List<FlashSaleItemEntity> findByFlashSaleId(Long flashSaleId);

    Optional<FlashSaleItemEntity> findByFlashSaleIdAndProductId(Long flashSaleId, String productId);
}

package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.inventory.domain.entity.Inventory;
import com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataInventoryRepository springDataInventoryRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = InventoryEntity.fromDomain(inventory);
        InventoryEntity savedEntity = springDataInventoryRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return springDataInventoryRepository.findByProductId(productId)
                .map(InventoryEntity::toDomain);
    }
}

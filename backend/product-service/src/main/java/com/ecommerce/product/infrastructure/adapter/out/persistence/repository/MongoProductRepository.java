package com.ecommerce.product.infrastructure.adapter.out.persistence.repository;

import com.ecommerce.product.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoProductRepository extends MongoRepository<ProductEntity, String> {
}

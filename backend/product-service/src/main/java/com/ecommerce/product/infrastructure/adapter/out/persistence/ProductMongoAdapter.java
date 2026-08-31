package com.ecommerce.product.infrastructure.adapter.out.persistence;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.out.ProductRepositoryPort;
import com.ecommerce.product.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.ecommerce.product.infrastructure.adapter.out.persistence.repository.MongoProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMongoAdapter implements ProductRepositoryPort {

    private final MongoProductRepository mongoRepository;

    @Override
    public List<Product> findAll() {
        return mongoRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findById(String id) {
        return mongoRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapToEntity(product);
        ProductEntity savedEntity = mongoRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    private Product mapToDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .discountPrice(entity.getDiscountPrice())
                .discountPercent(entity.getDiscountPercent())
                .imageUrl(entity.getImageUrl())
                .rating(entity.getRating())
                .soldCount(entity.getSoldCount())
                .stockCount(entity.getStockCount())
                .specs(entity.getSpecs())
                .isFlashSale(entity.getIsFlashSale())
                .build();
    }

    private ProductEntity mapToEntity(Product domain) {
        return ProductEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .category(domain.getCategory())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .discountPrice(domain.getDiscountPrice())
                .discountPercent(domain.getDiscountPercent())
                .imageUrl(domain.getImageUrl())
                .rating(domain.getRating())
                .soldCount(domain.getSoldCount())
                .stockCount(domain.getStockCount())
                .specs(domain.getSpecs())
                .isFlashSale(domain.getIsFlashSale())
                .build();
    }
}

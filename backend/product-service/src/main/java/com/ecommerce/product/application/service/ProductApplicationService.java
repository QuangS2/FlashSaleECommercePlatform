package com.ecommerce.product.application.service;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.in.ProductUseCase;
import com.ecommerce.product.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductApplicationService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }

    @Override
    public Optional<Product> getProductById(String id) {
        String cacheKey = "product:detail:" + id;
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null && !cachedJson.isBlank()) {
                Product product = objectMapper.readValue(cachedJson, Product.class);
                return Optional.ofNullable(product);
            }
        } catch (Exception ex) {
            // Fallback to database on cache error
        }

        Optional<Product> productOpt = productRepositoryPort.findById(id);
        if (productOpt.isPresent()) {
            try {
                String json = objectMapper.writeValueAsString(productOpt.get());
                redisTemplate.opsForValue().set(cacheKey, json, java.time.Duration.ofHours(1));
            } catch (Exception ex) {
                // Ignore cache write error
            }
        }
        return productOpt;
    }

    @Override
    public Product createProduct(Product product) {
        return productRepositoryPort.save(product);
    }

    @Override
    public Product incrementSoldCount(String id, int quantity) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + id));
        product.incrementSoldCount(quantity);
        return productRepositoryPort.save(product);
    }
}

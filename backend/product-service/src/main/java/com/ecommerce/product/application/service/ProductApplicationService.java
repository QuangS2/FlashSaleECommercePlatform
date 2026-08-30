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

    @Override
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }

    @Override
    public Optional<Product> getProductById(String id) {
        return productRepositoryPort.findById(id);
    }

    @Override
    public Product createProduct(Product product) {
        return productRepositoryPort.save(product);
    }
}

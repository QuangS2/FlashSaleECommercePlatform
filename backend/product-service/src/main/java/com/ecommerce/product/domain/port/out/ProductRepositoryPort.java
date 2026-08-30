package com.ecommerce.product.domain.port.out;

import com.ecommerce.product.domain.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    List<Product> findAll();
    Optional<Product> findById(String id);
    Product save(Product product);
}

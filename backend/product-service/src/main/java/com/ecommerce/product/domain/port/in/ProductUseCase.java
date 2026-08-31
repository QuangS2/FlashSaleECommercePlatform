package com.ecommerce.product.domain.port.in;

import com.ecommerce.product.domain.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductUseCase {
    List<Product> getAllProducts();
    Optional<Product> getProductById(String id);
    Product createProduct(Product product);
}

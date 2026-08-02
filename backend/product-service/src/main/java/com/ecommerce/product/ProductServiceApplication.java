package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @GetMapping("/api/v1/products")
    public List<Map<String, Object>> getProducts() {
        return List.of(
            Map.of("id", 1, "name", "iPhone 15 Pro Max Flash Sale", "price", 999.00, "stock", 100),
            Map.of("id", 2, "name", "MacBook Pro M3 Max", "price", 1999.00, "stock", 50)
        );
    }
}

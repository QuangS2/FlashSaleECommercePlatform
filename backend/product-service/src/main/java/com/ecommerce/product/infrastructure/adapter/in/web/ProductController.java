package com.ecommerce.product.infrastructure.adapter.in.web;

import com.ecommerce.product.application.dto.CreateProductRequest;
import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.in.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        List<Product> products = productUseCase.getAllProducts();

        List<ProductResponse> responses = products.stream()
                .filter(p -> matchesCategory(p, category))
                .filter(p -> matchesSearch(p, search))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return productUseCase.getProductById(id)
                .map(product -> ResponseEntity.ok(mapToResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .discountPercent(request.getDiscountPercent())
                .imageUrl(request.getImageUrl())
                .rating(request.getRating())
                .soldCount(request.getSoldCount())
                .stockCount(request.getStockCount())
                .specs(request.getSpecs())
                .isFlashSale(request.getIsFlashSale())
                .build();

        Product savedProduct = productUseCase.createProduct(product);
        return mapToResponse(savedProduct);
    }

    @PostMapping("/{id}/increment-sold")
    public ResponseEntity<ProductResponse> incrementSoldCount(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int quantity) {
        Product updatedProduct = productUseCase.incrementSoldCount(id, quantity);
        return ResponseEntity.ok(mapToResponse(updatedProduct));
    }

    private boolean matchesCategory(Product product, String category) {
        if (category == null || category.equalsIgnoreCase("Tất cả")) {
            return true;
        }
        return category.equalsIgnoreCase(product.getCategory());
    }

    private boolean matchesSearch(Product product, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }
        String s = search.toLowerCase().trim();
        boolean matchName = product.getName() != null && product.getName().toLowerCase().contains(s);
        boolean matchCat = product.getCategory() != null && product.getCategory().toLowerCase().contains(s);
        return matchName || matchCat;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .discountPercent(product.getDiscountPercent())
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .soldCount(product.getSoldCount())
                .stockCount(product.getStockCount())
                .specs(product.getSpecs())
                .isFlashSale(product.getIsFlashSale())
                .build();
    }
}

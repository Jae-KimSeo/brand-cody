package org.service.brandcody.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.dto.request.ProductRequest;
import org.service.brandcody.dto.response.CategoryPriceResponse;
import org.service.brandcody.dto.response.LowestPriceResponse;
import org.service.brandcody.dto.response.ProductResponse;
import org.service.brandcody.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductResponse>> getProductsByBrand(@PathVariable Long brandId) {
        List<ProductResponse> products = productService.getProductsByBrand(brandId).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @PostMapping("/brand/{brandId}")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Long brandId,
            @Valid @RequestBody ProductRequest request) {
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category is required for creating a new product");
        }
        Product product = productService.createProduct(brandId, request.getCategory(), request.getPrice());
        return new ResponseEntity<>(ProductResponse.from(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(id, request.getPrice());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PutMapping("/brand/{brandId}/category/{category}")
    public ResponseEntity<ProductResponse> updateProductByBrandAndCategory(
            @PathVariable Long brandId,
            @PathVariable String category,
            @Valid @RequestBody ProductRequest request) {
        Category categoryEnum = Category.fromDisplayName(category)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
        
        Product product = productService.updateProductByBrandAndCategory(brandId, categoryEnum, request.getPrice());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lowest-price")
    public ResponseEntity<LowestPriceResponse> getLowestPriceByAllCategories() {
        List<CategoryBrandPriceDto> lowestPrices = productService.findLowestPriceByAllCategories();
        int totalPrice = productService.calculateTotalLowestPriceAcrossCategories();
        
        LowestPriceResponse response = LowestPriceResponse.builder()
                .categories(lowestPrices)
                .totalPrice(totalPrice)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<CategoryPriceResponse> getCategoryPriceInfo(@PathVariable String category) {
        Category categoryEnum = Category.fromDisplayName(category)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
        
        List<CategoryBrandPriceDto> lowestPrices = productService.findLowestPriceByCategory(categoryEnum);
        List<CategoryBrandPriceDto> highestPrices = productService.findHighestPriceByCategory(categoryEnum);
        
        CategoryPriceResponse response = CategoryPriceResponse.from(category, lowestPrices, highestPrices);
        return ResponseEntity.ok(response);
    }
}
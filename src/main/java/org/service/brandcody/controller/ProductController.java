package org.service.brandcody.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.dto.request.ProductRequest;
import org.service.brandcody.dto.response.CategoryPriceResponse;
import org.service.brandcody.dto.response.LowestPriceResponse;
import org.service.brandcody.dto.response.ProductResponse;
import org.service.brandcody.exception.ErrorResponse;
import org.service.brandcody.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "상품 API", description = "상품 정보 조회 및 관리를 위한 API")
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "모든 상품 조회", description = "시스템에 등록된 모든 상품 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "상품 상세 조회", description = "특정 ID의 상품 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 조회 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @Operation(summary = "브랜드별 상품 조회", description = "특정 브랜드의 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductResponse>> getProductsByBrand(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId) {
        List<ProductResponse> products = productService.getProductsByBrand(brandId).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "새 상품 등록", description = "특정 브랜드에 새로운 상품을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상품 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/brand/{brandId}")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId,
            @Parameter(description = "상품 정보", required = true) @Valid @RequestBody ProductRequest request) {
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category is required for creating a new product");
        }
        Product product = productService.createProduct(brandId, request.getCategory(), request.getPrice());
        return new ResponseEntity<>(ProductResponse.from(product), HttpStatus.CREATED);
    }

    @Operation(summary = "상품 정보 수정", description = "기존 상품의 가격을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정할 상품 정보", required = true) @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(id, request.getPrice());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @Operation(summary = "브랜드-카테고리 상품 수정", 
            description = "특정 브랜드의 특정 카테고리 상품 가격을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/brand/{brandId}/category/{category}")
    public ResponseEntity<ProductResponse> updateProductByBrandAndCategory(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId,
            @Parameter(description = "카테고리명", required = true) @PathVariable String category,
            @Parameter(description = "수정할 상품 정보", required = true) @Valid @RequestBody ProductRequest request) {
        Category categoryEnum = Category.fromDisplayName(category)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
        
        Product product = productService.updateProductByBrandAndCategory(brandId, categoryEnum, request.getPrice());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @Operation(summary = "상품 삭제", description = "시스템에서 특정 상품을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "카테고리별 최저가격 조회", 
            description = "각 카테고리별 최저가격 브랜드와 상품 가격, 총액을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최저가격 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/lowest-price")
    public ResponseEntity<LowestPriceResponse> getLowestPriceByAllCategories() {
        List<CategoryBrandPriceDto> lowestPrices = productService.findLowestPriceByAllCategories();

        int totalPrice = lowestPrices.stream()
                .mapToInt(CategoryBrandPriceDto::getPrice)
                .sum();
        
        LowestPriceResponse response = LowestPriceResponse.builder()
                .categories(lowestPrices)
                .totalPrice(totalPrice)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 최저/최고가격 조회", 
            description = "특정 카테고리의 최저가격 브랜드와 최고가격 브랜드, 가격을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 가격 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<CategoryPriceResponse> getCategoryPriceInfo(
            @Parameter(description = "카테고리명", required = true) @PathVariable String category) {
        Category categoryEnum = Category.fromDisplayName(category)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
        
        List<CategoryBrandPriceDto> lowestPrices = productService.findLowestPriceByCategory(categoryEnum);
        List<CategoryBrandPriceDto> highestPrices = productService.findHighestPriceByCategory(categoryEnum);
        
        CategoryPriceResponse response = CategoryPriceResponse.from(category, lowestPrices, highestPrices);
        return ResponseEntity.ok(response);
    }
}
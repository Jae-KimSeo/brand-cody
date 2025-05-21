package org.service.brandcody.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Product;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "상품 응답 모델")
public class ProductResponse {
    @Schema(description = "상품 ID", example = "1")
    private Long id;
    
    @Schema(description = "브랜드명", example = "TestA")
    private String brand;
    
    @Schema(description = "카테고리명", example = "상의")
    private String category;
    
    @Schema(description = "상품 가격", example = "10000")
    private Integer price;
    
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .brand(product.getBrand().getName())
                .category(product.getCategory().getDisplayName())
                .price(product.getPrice())
                .build();
    }
}
package org.service.brandcody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Product;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String brand;
    private String category;
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
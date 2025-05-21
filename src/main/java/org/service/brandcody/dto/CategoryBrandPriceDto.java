package org.service.brandcody.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.service.brandcody.domain.Category;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리별 브랜드 가격 정보 모델")
public class CategoryBrandPriceDto {
    @Schema(description = "카테고리 정보", example = "TOP")
    private Category category;
    
    @Schema(description = "브랜드명", example = "TestA")
    private String brandName;
    
    @Schema(description = "가격", example = "10000")
    private Integer price;

    @Schema(description = "카테고리 표시명", example = "상의")
    public String getCategoryDisplayName() {
        return category.getDisplayName();
    }

    @Schema(description = "포맷된 가격", example = "10,000")
    public String getFormattedPrice() {
        return String.format("%,d", price);
    }
}

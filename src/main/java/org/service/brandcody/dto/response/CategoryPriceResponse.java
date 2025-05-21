package org.service.brandcody.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.dto.CategoryBrandPriceDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "카테고리별 최저/최고가격 응답 모델")
public class CategoryPriceResponse {
    @Schema(description = "카테고리명", example = "상의")
    private String category;
    
    @Schema(description = "최저가 브랜드 정보")
    private Map<String, Object> min;
    
    @Schema(description = "최고가 브랜드 정보")
    private Map<String, Object> max;
    
    public static CategoryPriceResponse from(
            String categoryDisplayName, 
            List<CategoryBrandPriceDto> lowestPriceDtos, 
            List<CategoryBrandPriceDto> highestPriceDtos) {
        
        CategoryBrandPriceDto lowestDto = lowestPriceDtos.isEmpty() ? null : lowestPriceDtos.get(0);
        CategoryBrandPriceDto highestDto = highestPriceDtos.isEmpty() ? null : highestPriceDtos.get(0);
        
        Map<String, Object> min = new HashMap<>();
        Map<String, Object> max = new HashMap<>();
        
        if (lowestDto != null) {
            min.put("brand", lowestDto.getBrandName());
            min.put("price", lowestDto.getPrice());
            min.put("formattedPrice", lowestDto.getFormattedPrice());
        }
        
        if (highestDto != null) {
            max.put("brand", highestDto.getBrandName());
            max.put("price", highestDto.getPrice());
            max.put("formattedPrice", highestDto.getFormattedPrice());
        }
        
        return CategoryPriceResponse.builder()
                .category(categoryDisplayName)
                .min(min)
                .max(max)
                .build();
    }
}
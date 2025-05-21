package org.service.brandcody.dto.response;

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
public class CategoryPriceResponse {
    private String category;
    private Map<String, Object> min;
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
        }
        
        if (highestDto != null) {
            max.put("brand", highestDto.getBrandName());
            max.put("price", highestDto.getPrice());
        }
        
        return CategoryPriceResponse.builder()
                .category(categoryDisplayName)
                .min(min)
                .max(max)
                .build();
    }
}
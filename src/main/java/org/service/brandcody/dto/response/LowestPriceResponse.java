package org.service.brandcody.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.dto.CategoryBrandPriceDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "카테고리별 최저가격 응답 모델")
public class LowestPriceResponse {
    @Schema(description = "카테고리별 최저가격 정보 목록")
    private List<CategoryBrandPriceDto> categories;
    
    @Schema(description = "최저가 총액", example = "35000")
    private int totalPrice;
    
    @Schema(description = "포맷된 총액", example = "35,000")
    public String getFormattedTotalPrice() {
        return String.format("%,d", totalPrice);
    }
}
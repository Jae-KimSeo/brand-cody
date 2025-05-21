package org.service.brandcody.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.text.NumberFormat;
import java.util.Locale;

@Schema(description = "상품 항목 정보")
public record ItemDto(
    @Schema(description = "카테고리명", example = "상의") String category,
    @Schema(description = "가격", example = "10000") int price,
    @Schema(description = "포맷된 가격", example = "10,000원") String formattedPrice
) {
    public static ItemDto of(String category, int price) {
        return new ItemDto(category, price, formatPrice(price));
    }
    
    public static String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }
}

package org.service.brandcody.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.dto.ItemDto;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "단일 브랜드 최저가 응답 모델")
public class SingleBrandResponse {
    @Schema(description = "브랜드명", example = "TestA")
    private String brand;
    
    @Schema(description = "카테고리별 상품 정보 목록")
    private List<ItemDto> items;
    
    @Schema(description = "총 가격", example = "35000")
    private int totalPrice;
    
    @Schema(description = "포맷된 총 가격", example = "35,000원")
    private String formattedTotalPrice;
    
    public static SingleBrandResponse from(Brand brand) {
        return from(brand, brand.getTotalPrice());
    }
    
    public static SingleBrandResponse from(Brand brand, Integer calculatedTotalPrice) {
        List<ItemDto> items = Arrays.stream(Category.values())
            .map(brand::getCheapestProductByCategory)
            .filter(Objects::nonNull)
            .map(p -> new ItemDto(
                    p.getCategory().getDisplayName(),
                    p.getPrice(),
                    formatPrice(p.getPrice())))
            .toList();

        return SingleBrandResponse.builder()
                .brand(brand.getName())
                .items(items)
                .totalPrice(calculatedTotalPrice)
                .formattedTotalPrice(formatPrice(calculatedTotalPrice))
                .build();
    }
    
    private static String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }
}
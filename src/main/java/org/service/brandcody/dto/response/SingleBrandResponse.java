package org.service.brandcody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.ItemDto;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public class SingleBrandResponse {
    private String brand;
    private List<ItemDto> items;
    private int totalPrice;
    private String formattedTotalPrice;
    
    public static SingleBrandResponse from(Brand brand) {
        return from(brand, brand.getTotalPrice());
    }
    
    public static SingleBrandResponse from(Brand brand, Integer calculatedTotalPrice) {
        List<ItemDto> items = Arrays.stream(Category.values())
            .map(brand::getProductByCategory)
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
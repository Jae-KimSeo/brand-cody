package org.service.brandcody.dto;

import java.text.NumberFormat;
import java.util.Locale;

public record ItemDto(String category, int price, String formattedPrice) {
    public static ItemDto of(String category, int price) {
        return new ItemDto(category, price, formatPrice(price));
    }
    
    public static String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }
}

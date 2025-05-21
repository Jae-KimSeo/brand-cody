package org.service.brandcody.dto;

import org.service.brandcody.domain.Brand;
import org.service.brandcody.dto.response.SingleBrandResponse;

import java.text.NumberFormat;
import java.util.Locale;

public record BrandTotalProjection(Brand brand, int totalPrice) {
    public static BrandTotalProjection of(Brand brand, int totalPrice) {
        return new BrandTotalProjection(brand, totalPrice);
    }

    public String getFormattedTotalPrice() {
        return formatPrice(totalPrice);
    }

    private static String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }

    public SingleBrandResponse toResponse() {
        return SingleBrandResponse.from(brand, totalPrice);
    }
}

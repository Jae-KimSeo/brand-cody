package org.service.brandcody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class SingleBrandResponse {
    private String brand;
    private Map<String, Object> items;
    private int totalPrice;
    private String formattedTotalPrice;
    
    public static SingleBrandResponse from(Brand brand) {
        return from(brand, brand.getTotalPrice());
    }
    
    public static SingleBrandResponse from(Brand brand, Integer calculatedTotalPrice) {
        Map<String, Object> items = new HashMap<>();
        
        for (Category category : Category.values()) {
            Product product = brand.getProductByCategory(category);
            if (product != null) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("price", product.getPrice());
                itemInfo.put("formattedPrice", formatPrice(product.getPrice()));
                items.put(category.getDisplayName(), itemInfo);
            }
        }
        
        return SingleBrandResponse.builder()
                .brand(brand.getName())
                .items(items)
                .totalPrice(calculatedTotalPrice)
                .formattedTotalPrice(formatPrice(calculatedTotalPrice))
                .build();
    }
    
    private static String formatPrice(Integer price) {
        return String.format("%,d원", price);
    }
}
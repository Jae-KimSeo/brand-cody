package org.service.brandcody.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.service.brandcody.domain.Category;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBrandPriceDto {
    private Category category;
    private String brandName;
    private Integer price;

    public String getCategoryDisplayName() {
        return category.getDisplayName();
    }

    public String getFormattedPrice() {
        return String.format("%,d", price);
    }
}

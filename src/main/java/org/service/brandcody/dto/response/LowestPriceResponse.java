package org.service.brandcody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.dto.CategoryBrandPriceDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class LowestPriceResponse {
    private List<CategoryBrandPriceDto> categories;
    private int totalPrice;
}
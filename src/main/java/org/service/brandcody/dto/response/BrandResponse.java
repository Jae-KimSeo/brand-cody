package org.service.brandcody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Brand;

@Getter
@Builder
@AllArgsConstructor
public class BrandResponse {
    private Long id;
    private String name;
    
    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}
package org.service.brandcody.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.service.brandcody.domain.Brand;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "브랜드 응답 모델")
public class BrandResponse {
    @Schema(description = "브랜드 ID", example = "1")
    private Long id;
    
    @Schema(description = "브랜드명", example = "TestA")
    private String name;
    
    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}
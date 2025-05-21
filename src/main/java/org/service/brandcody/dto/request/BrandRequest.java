package org.service.brandcody.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "브랜드 등록/수정 요청 모델")
public class BrandRequest {
    @NotBlank(message = "Brand name cannot be empty")
    @Schema(description = "브랜드명", example = "NewBrand", required = true)
    private String name;
}
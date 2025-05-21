package org.service.brandcody.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.service.brandcody.domain.Category;

@Getter
@NoArgsConstructor
@Schema(description = "상품 등록/수정 요청 모델")
public class ProductRequest {
    @Schema(description = "카테고리 (상품 생성시 필수, 수정시 옵션)", example = "TOP", 
        required = false, enumAsRef = true)
    private Category category;
    
    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be at least 0")
    @Schema(description = "상품 가격", example = "10000", required = true, minimum = "0")
    private Integer price;
}
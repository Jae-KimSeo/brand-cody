package org.service.brandcody.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.service.brandcody.domain.Category;

@Getter
@NoArgsConstructor
public class ProductRequest {
    private Category category;
    
    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be at least 0")
    private Integer price;
}
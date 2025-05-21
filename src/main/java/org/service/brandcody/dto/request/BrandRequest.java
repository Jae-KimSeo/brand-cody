package org.service.brandcody.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandRequest {
    @NotBlank(message = "Brand name cannot be empty")
    private String name;
}
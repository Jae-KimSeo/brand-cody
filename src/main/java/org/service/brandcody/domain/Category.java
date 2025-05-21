package org.service.brandcody.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.Optional;

@Schema(description = "의류 카테고리", enumAsRef = true)
public enum Category {
    @Schema(description = "상의")
    TOP("상의"),
    
    @Schema(description = "아우터")
    OUTER("아우터"),
    
    @Schema(description = "바지")
    PANTS("바지"),
    
    @Schema(description = "스니커즈")
    SNEAKERS("스니커즈"),
    
    @Schema(description = "가방")
    BAG("가방"),
    
    @Schema(description = "모자")
    HAT("모자"),
    
    @Schema(description = "양말")
    SOCKS("양말"),
    
    @Schema(description = "액세서리")
    ACCESSORY("액세서리");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Category> fromDisplayName(String name) {
        // 먼저 enum 이름(영문)으로 검색
        try {
            return Optional.of(Category.valueOf(name));
        } catch (IllegalArgumentException e) {
            // enum 이름으로 찾지 못한 경우, 표시 이름(한글)으로 검색
            return Arrays.stream(values())
                    .filter(category -> category.getDisplayName().equals(name))
                    .findFirst();
        }
    }
}

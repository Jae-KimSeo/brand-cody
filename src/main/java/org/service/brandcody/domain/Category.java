package org.service.brandcody.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Category {
    TOP("상의"),
    OUTER("아우터"),
    PANTS("바지"),
    SNEAKERS("스니커즈"),
    BAG("가방"),
    HAT("모자"),
    SOCKS("양말"),
    ACCESSORY("액세서리");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Category> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(category -> category.getDisplayName().equals(displayName))
                .findFirst();
    }
}

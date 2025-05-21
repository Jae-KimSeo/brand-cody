package org.service.brandcody.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_brand_category", columnList = "brand_id, category"),
        @Index(name = "idx_product_category_price", columnList = "category, price")
})

@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Integer price;

    public Product(Category category, Integer price) {
        this.category = category;
        this.price = price;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }
}

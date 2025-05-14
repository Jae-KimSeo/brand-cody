package org.service.brandcody.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands", indexes = {
        @Index(name = "idx_brand_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Brand {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Transient
    public Product getProductByCategory(Category category) {
        return products.stream()
                .filter(product -> product.getCategory() == category)
                .findFirst()
                .orElse(null);
    }

    @Transient
    public Integer getTotalPrice() {
        return products.stream()
                .mapToInt(Product::getPrice)
                .sum();
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setBrand(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setBrand(null);
    }
}

package org.service.brandcody.repository;

import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBrandIdOrderByCategory(Long brandId);
    List<Product> findByBrandIdAndCategory(Long brandId, Category category);
    
    default Optional<Product> findCheapestProductByBrandAndCategory(Long brandId, Category category) {
        return findByBrandIdAndCategory(brandId, category).stream()
                .min(Comparator.comparing(Product::getPrice));
    }
    
    // 동시성 테스트를 위한 카운트 메서드
    long countByBrandIdAndCategory(Long brandId, Category category);

    // 모든 카테고리에 대해 최저가격 브랜드와 가격 조회 쿼리
    @Query("SELECT new org.service.brandcody.dto.CategoryBrandPriceDto(p.category, b.name, p.price) FROM Product p JOIN p.brand b " +
            "WHERE (p.category, p.price) IN (SELECT p2.category, MIN(p2.price) FROM Product p2 GROUP BY p2.category)")
    List<CategoryBrandPriceDto> findLowestPriceByCategory();

    // 특정 카테고리에서 최저가격 브랜드와 가격 조회 쿼리
    @Query("SELECT new org.service.brandcody.dto.CategoryBrandPriceDto(p.category, b.name, p.price) FROM Product p JOIN p.brand b " +
            "WHERE p.category = :category AND p.price = (SELECT MIN(p2.price) FROM Product p2 WHERE p2.category = :category)")
    List<CategoryBrandPriceDto> findLowestPriceByCategory(@Param("category") Category category);

    // 특정 카테고리에서 최고가격 브랜드와 가격 조회 쿼리
    @Query("SELECT new org.service.brandcody.dto.CategoryBrandPriceDto(p.category, b.name, p.price) FROM Product p JOIN p.brand b " +
            "WHERE p.category = :category AND p.price = (SELECT MAX(p2.price) FROM Product p2 WHERE p2.category = :category)")
    List<CategoryBrandPriceDto> findHighestPriceByCategory(@Param("category") Category category);
}

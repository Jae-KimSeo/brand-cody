package org.service.brandcody.repository;

import org.service.brandcody.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);

    // 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격 브랜드를 찾는 쿼리
    @Query("SELECT b, SUM(p.price) as total FROM Brand b JOIN b.products p GROUP BY b " +
            "HAVING COUNT(DISTINCT p.category) = 8" +
           "ORDER BY total ASC LIMIT 1")
    Object[] findBrandWithLowestTotalPrice();

    // 모든 브랜드와 그 브랜드의 상품 총액 조회 쿼리
    @Query("SELECT b, SUM(p.price) as total FROM Brand b JOIN b.products p GROUP BY b " +
            "HAVING COUNT(DISTINCT p.category) = 8" +
            "ORDER BY total ASC")
    List<Object> findAllBrandsWithTotalPrice();
}

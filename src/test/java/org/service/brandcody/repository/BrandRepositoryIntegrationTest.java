package org.service.brandcody.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.BrandTotalProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.service.brandcody.config.TestConfig;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BrandRepositoryIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Rollback
    @DisplayName("findBrandWithLowestTotalPrice 메서드가 올바른 브랜드와 총액을 반환하는지 확인")
    void findBrandWithLowestTotalPrice_ReturnsCorrectBrandAndTotalPrice() {
        // Given: Create test brands with products in all categories
        Brand cheapestBrand = createBrandWithProducts("CheapestBrand", 
            9000,  // TOP
            5000,  // OUTER
            3000,  // PANTS
            8500,  // SNEAKERS
            1800,  // BAG
            1200,  // HAT
            1500,  // SOCKS
            1700   // ACCESSORY
        );
        
        Brand expensiveBrand = createBrandWithProducts("ExpensiveBrand", 
            10000, // TOP
            5500,  // OUTER
            3200,  // PANTS
            9000,  // SNEAKERS
            2000,  // BAG
            1500,  // HAT
            1800,  // SOCKS
            2000   // ACCESSORY
        );

        Brand mediumBrand = createBrandWithProducts("MediumBrand", 
            9500,  // TOP
            5200,  // OUTER
            3100,  // PANTS
            8800,  // SNEAKERS
            1900,  // BAG
            1300,  // HAT
            1600,  // SOCKS
            1800   // ACCESSORY
        );

        // Create incomplete brand (missing some categories)
        Brand incompleteBrand = new Brand();
        incompleteBrand.setName("IncompleteBrand");
        brandRepository.save(incompleteBrand);
        
        // Add only 4 categories to incomplete brand
        Product p1 = new Product(Category.TOP, 8000);
        Product p2 = new Product(Category.OUTER, 4500);
        Product p3 = new Product(Category.PANTS, 2800);
        Product p4 = new Product(Category.SNEAKERS, 8000);
        
        incompleteBrand.addProduct(p1);
        incompleteBrand.addProduct(p2);
        incompleteBrand.addProduct(p3);
        incompleteBrand.addProduct(p4);
        
        productRepository.saveAll(Arrays.asList(p1, p2, p3, p4));
        
        // Flush and clear to ensure all entities are persisted
        entityManager.flush();
        entityManager.clear();

        // Calculate expected total
        int expectedCheapestTotal = 9000 + 5000 + 3000 + 8500 + 1800 + 1200 + 1500 + 1700; // 31700
        
        // When
        List<BrandTotalProjection> result = brandRepository.findBrandWithLowestTotalPrice(PageRequest.of(0, 1));
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        
        BrandTotalProjection lowestBrand = result.get(0);
        assertThat(lowestBrand.brand().getName()).isEqualTo("CheapestBrand");
        assertThat(lowestBrand.totalPrice()).isEqualTo(expectedCheapestTotal);
    }

    @Test
    @Rollback
    @DisplayName("브랜드 이름 UNIQUE 제약 조건 위반 시 DataIntegrityViolationException이 발생하는지 검증")
    void saveBrand_WithDuplicateName_ThrowsDataIntegrityViolationException() {
        // Given
        String brandName = "UniqueBrandName";
        
        Brand brand1 = new Brand();
        brand1.setName(brandName);
        brandRepository.save(brand1);
        
        // Flush to ensure the first brand is persisted
        entityManager.flush();
        
        // When & Then
        Brand brand2 = new Brand();
        brand2.setName(brandName);
        
        assertThatThrownBy(() -> {
            brandRepository.save(brand2);
            entityManager.flush(); // Flush to trigger the constraint violation
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Helper method to create a brand with products in all categories
     */
    private Brand createBrandWithProducts(String brandName, int topPrice, int outerPrice, int pantsPrice, 
                                         int sneakersPrice, int bagPrice, int hatPrice, int socksPrice, 
                                         int accessoryPrice) {
        Brand brand = new Brand();
        brand.setName(brandName);
        brandRepository.save(brand);
        
        Product top = new Product(Category.TOP, topPrice);
        Product outer = new Product(Category.OUTER, outerPrice);
        Product pants = new Product(Category.PANTS, pantsPrice);
        Product sneakers = new Product(Category.SNEAKERS, sneakersPrice);
        Product bag = new Product(Category.BAG, bagPrice);
        Product hat = new Product(Category.HAT, hatPrice);
        Product socks = new Product(Category.SOCKS, socksPrice);
        Product accessory = new Product(Category.ACCESSORY, accessoryPrice);
        
        brand.addProduct(top);
        brand.addProduct(outer);
        brand.addProduct(pants);
        brand.addProduct(sneakers);
        brand.addProduct(bag);
        brand.addProduct(hat);
        brand.addProduct(socks);
        brand.addProduct(accessory);
        
        productRepository.saveAll(Arrays.asList(top, outer, pants, sneakers, bag, hat, socks, accessory));
        
        return brand;
    }
}
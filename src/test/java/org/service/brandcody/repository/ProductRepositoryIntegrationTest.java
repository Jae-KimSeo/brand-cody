package org.service.brandcody.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.service.brandcody.config.TestConfig;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private EntityManager entityManager;

    private Brand brandA;
    private Brand brandB;
    private Brand brandC;
    private Brand brandD;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        productRepository.deleteAll();
        brandRepository.deleteAll();

        // Create test brands
        brandA = new Brand();
        brandA.setName("BrandA");
        brandRepository.save(brandA);

        brandB = new Brand();
        brandB.setName("BrandB");
        brandRepository.save(brandB);

        brandC = new Brand();
        brandC.setName("BrandC");
        brandRepository.save(brandC);

        brandD = new Brand();
        brandD.setName("BrandD");
        brandRepository.save(brandD);

        // Create products with varying prices for each category and brand
        // TOP category: BrandB has lowest, BrandD has highest
        createProduct(brandA, Category.TOP, 10000);
        createProduct(brandB, Category.TOP, 9000);  // Lowest
        createProduct(brandC, Category.TOP, 10500);
        createProduct(brandD, Category.TOP, 11000); // Highest

        // OUTER category: BrandA and BrandD tied for lowest, BrandC highest
        createProduct(brandA, Category.OUTER, 5000); // Tied lowest
        createProduct(brandB, Category.OUTER, 5500);
        createProduct(brandC, Category.OUTER, 6000); // Highest
        createProduct(brandD, Category.OUTER, 5000); // Tied lowest

        // PANTS category: BrandC has lowest, BrandA has highest
        createProduct(brandA, Category.PANTS, 4000); // Highest
        createProduct(brandB, Category.PANTS, 3500);
        createProduct(brandC, Category.PANTS, 3000); // Lowest
        createProduct(brandD, Category.PANTS, 3200);

        // SNEAKERS category: BrandA and BrandC tied for lowest, BrandD highest
        createProduct(brandA, Category.SNEAKERS, 8500); // Tied lowest
        createProduct(brandB, Category.SNEAKERS, 9000);
        createProduct(brandC, Category.SNEAKERS, 8500); // Tied lowest
        createProduct(brandD, Category.SNEAKERS, 9500); // Highest

        // Flush to ensure all entities are persisted
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Rollback
    @DisplayName("findLowestPriceByCategory가 올바른 결과를 반환하는지 확인")
    void findLowestPriceByCategory_ReturnsCorrectResults() {
        // When
        List<CategoryBrandPriceDto> results = productRepository.findLowestPriceByCategory();
        
        // Convert results to map for easier assertion
        Map<Category, List<CategoryBrandPriceDto>> resultsByCategory = results.stream()
            .collect(Collectors.groupingBy(CategoryBrandPriceDto::getCategory));
        
        // Then
        // TOP category: BrandB should have lowest price (9000)
        List<CategoryBrandPriceDto> topResults = resultsByCategory.get(Category.TOP);
        assertThat(topResults).hasSize(1);
        assertThat(topResults.get(0).getBrandName()).isEqualTo("BrandB");
        assertThat(topResults.get(0).getPrice()).isEqualTo(9000);
        
        // OUTER category: BrandA and BrandD tied for lowest (5000)
        List<CategoryBrandPriceDto> outerResults = resultsByCategory.get(Category.OUTER);
        assertThat(outerResults).hasSize(2);
        assertThat(outerResults).extracting("brandName")
            .containsExactlyInAnyOrder("BrandA", "BrandD");
        assertThat(outerResults).extracting("price")
            .containsOnly(5000);
        
        // PANTS category: BrandC should have lowest price (3000)
        List<CategoryBrandPriceDto> pantsResults = resultsByCategory.get(Category.PANTS);
        assertThat(pantsResults).hasSize(1);
        assertThat(pantsResults.get(0).getBrandName()).isEqualTo("BrandC");
        assertThat(pantsResults.get(0).getPrice()).isEqualTo(3000);
        
        // SNEAKERS category: BrandA and BrandC tied for lowest (8500)
        List<CategoryBrandPriceDto> sneakersResults = resultsByCategory.get(Category.SNEAKERS);
        assertThat(sneakersResults).hasSize(2);
        assertThat(sneakersResults).extracting("brandName")
            .containsExactlyInAnyOrder("BrandA", "BrandC");
        assertThat(sneakersResults).extracting("price")
            .containsOnly(8500);
    }

    @Test
    @Rollback
    @DisplayName("findHighestPriceByCategory가 올바른 결과를 반환하는지 확인")
    void findHighestPriceByCategory_ReturnsCorrectResults() {
        // When
        List<CategoryBrandPriceDto> topResults = productRepository.findHighestPriceByCategory(Category.TOP);
        List<CategoryBrandPriceDto> outerResults = productRepository.findHighestPriceByCategory(Category.OUTER);
        List<CategoryBrandPriceDto> pantsResults = productRepository.findHighestPriceByCategory(Category.PANTS);
        List<CategoryBrandPriceDto> sneakersResults = productRepository.findHighestPriceByCategory(Category.SNEAKERS);
        
        // Then
        // TOP category: BrandD should have highest price (11000)
        assertThat(topResults).hasSize(1);
        assertThat(topResults.getFirst().getBrandName()).isEqualTo("BrandD");
        assertThat(topResults.getFirst().getPrice()).isEqualTo(11000);
        
        // OUTER category: BrandC should have highest price (6000)
        assertThat(outerResults).hasSize(1);
        assertThat(outerResults.getFirst().getBrandName()).isEqualTo("BrandC");
        assertThat(outerResults.getFirst().getPrice()).isEqualTo(6000);
        
        // PANTS category: BrandA should have highest price (4000)
        assertThat(pantsResults).hasSize(1);
        assertThat(pantsResults.getFirst().getBrandName()).isEqualTo("BrandA");
        assertThat(pantsResults.getFirst().getPrice()).isEqualTo(4000);
        
        // SNEAKERS category: BrandD should have highest price (9500)
        assertThat(sneakersResults).hasSize(1);
        assertThat(sneakersResults.getFirst().getBrandName()).isEqualTo("BrandD");
        assertThat(sneakersResults.getFirst().getPrice()).isEqualTo(9500);
    }

    @Test
    @Rollback
    @DisplayName("동점인 경우 모든 해당 행이 반환되는지 검증")
    void findLowestPriceByCategory_WithTies_ReturnsAllTiedRows() {
        // Given
        // Create another brand with same prices as some existing ones to create more ties
        Brand brandE = new Brand();
        brandE.setName("BrandE");
        brandRepository.save(brandE);
        
        // Create product with same lowest price for TOP (9000, same as BrandB)
        createProduct(brandE, Category.TOP, 9000);
        
        // Create product with same lowest price for PANTS (3000, same as BrandC)
        createProduct(brandE, Category.PANTS, 3000);
        
        // Flush to ensure all entities are persisted
        entityManager.flush();
        entityManager.clear();
        
        // When
        List<CategoryBrandPriceDto> topResults = productRepository.findLowestPriceByCategory(Category.TOP);
        List<CategoryBrandPriceDto> pantsResults = productRepository.findLowestPriceByCategory(Category.PANTS);
        
        // Then
        // TOP category: BrandB and BrandE tied for lowest price (9000)
        assertThat(topResults).hasSize(2);
        assertThat(topResults).extracting("brandName")
            .containsExactlyInAnyOrder("BrandB", "BrandE");
        assertThat(topResults).extracting("price")
            .containsOnly(9000);
        
        // PANTS category: BrandC and BrandE tied for lowest price (3000)
        assertThat(pantsResults).hasSize(2);
        assertThat(pantsResults).extracting("brandName")
            .containsExactlyInAnyOrder("BrandC", "BrandE");
        assertThat(pantsResults).extracting("price")
            .containsOnly(3000);
    }

    /**
     * Helper method to create a product
     */
    private Product createProduct(Brand brand, Category category, int price) {
        Product product = new Product(category, price);
        brand.addProduct(product);
        return productRepository.save(product);
    }
}
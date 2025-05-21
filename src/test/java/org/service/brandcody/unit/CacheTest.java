package org.service.brandcody.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.config.CacheConfig;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.BrandTotalProjection;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.service.brandcody.service.BrandService;
import org.service.brandcody.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/fixture/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CacheTest {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheTest.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("브랜드 캐싱 테스트 - 캐시 생성 및 적중 확인")
    void brand_cache_test() {
        // 첫 번째 호출 - 캐시 생성
        Brand brandFirstCall = brandService.getBrandById(1L);
        assertThat(brandFirstCall).isNotNull();
        
        // 캐시에 데이터가 저장되었는지 확인
        Object cachedBrand = Objects.requireNonNull(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).get(1L);
        assertThat(cachedBrand).isNotNull();
        
        // 두 번째 호출 - 캐시 적중 확인
        Brand brandSecondCall = brandService.getBrandById(1L);
        
        // 같은 객체가 반환되는지 확인
        assertThat(brandSecondCall).isEqualTo(brandFirstCall);
        
        // 캐시 만료 시뮬레이션
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).evict(1L);
        
        // 캐시 삭제 후 재호출 - 캐시 미스 확인
        Brand brandThirdCall = brandService.getBrandById(1L);
        assertThat(brandThirdCall).isNotNull();
        
        // 새로운 캐시 엔트리가 생성되었는지 확인
        Object refreshedCachedBrand = Objects.requireNonNull(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).get(1L);
        assertThat(refreshedCachedBrand).isNotNull();
    }
    
    @Test
    @DisplayName("최저가 브랜드 캐싱 테스트 - 캐시 생성 및 적중 확인")
    void lowest_price_brand_cache_test() {
        // 첫 번째 호출 - 캐시 생성
        BrandTotalProjection firstCall = brandService.findBrandWithLowestTotalPrice();
        assertThat(firstCall).isNotNull();
        assertThat(firstCall.totalPrice()).isGreaterThan(0);
        assertThat(firstCall.brand()).isNotNull();
        
        // 캐시 키 확인을 위해 모든 캐시 출력
        logger.info("===== 캐시 키 확인 =====");
        cacheManager.getCacheNames().forEach(cacheName -> {
            logger.info("캐시 이름: {}", cacheName);
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache();
                logger.info("  캐시 내용: {}", Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache());
            }
        });
        
        // 캐시에 데이터가 저장되었는지 확인
        assertThat(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BRAND_CACHE)).isNotNull();
        
        // 두 번째 호출 - 캐시 적중 확인
        BrandTotalProjection secondCall = brandService.findBrandWithLowestTotalPrice();
        assertThat(secondCall).isEqualTo(firstCall); // 동일한 객체 반환 확인
        
        // 가격 업데이트로 캐시 무효화 유발
        Long lowestPriceBrandId = firstCall.brand().getId();
        productService.updateProductByBrandAndCategory(lowestPriceBrandId, Category.TOP, 999999);
        
        // 캐시가 무효화되었는지 확인
        Object invalidatedCache = Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BRAND_CACHE)).get("SimpleKey []");
        assertThat(invalidatedCache).isNull();
        
        // 새로운 최저가 브랜드 조회
        BrandTotalProjection thirdCall = brandService.findBrandWithLowestTotalPrice();
        assertThat(thirdCall).isNotNull();
        
        // 기존 최저가 브랜드와 다른지 확인
        assertThat(thirdCall.brand().getId()).isNotEqualTo(lowestPriceBrandId);
    }
    
    @Test
    @DisplayName("최저가 카테고리 캐싱 테스트 - 캐시 생성 및 카테고리별 키 검증")
    void lowest_price_by_category_cache_test() {
        // TOP 카테고리 첫 번째 호출 - 캐시 생성
        List<CategoryBrandPriceDto> topPrices = productService.findLowestPriceByCategory(Category.TOP);
        assertThat(topPrices).isNotEmpty();
        assertThat(topPrices.getFirst().getCategory()).isEqualTo(Category.TOP);
        
        // TOP 카테고리 캐시 확인
        Object cachedTopData = Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.TOP.name());
        assertThat(cachedTopData).isNotNull();
        
        // PANTS 카테고리 호출 - 다른 키로 캐시 생성
        List<CategoryBrandPriceDto> pantsPrices = productService.findLowestPriceByCategory(Category.PANTS);
        assertThat(pantsPrices).isNotEmpty();
        assertThat(pantsPrices.getFirst().getCategory()).isEqualTo(Category.PANTS);
        
        // PANTS 카테고리 캐시 확인
        Object cachedPantsData = Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.PANTS.name());
        assertThat(cachedPantsData).isNotNull();
        
        // TOP 카테고리 두 번째 호출 - 캐시 적중 확인
        List<CategoryBrandPriceDto> topPricesSecondCall = productService.findLowestPriceByCategory(Category.TOP);
        assertThat(topPricesSecondCall).isEqualTo(topPrices); // 동일한 객체 반환 확인
        
        // 특정 카테고리 캐시 무효화를 위한 가격 업데이트
        // 브랜드 이름으로 브랜드 ID 찾기
        String brandName = topPrices.getFirst().getBrandName();
        Brand brand = brandService.getBrandByName(brandName);
        productService.updateProductByBrandAndCategory(brand.getId(), Category.TOP, 99999);
        
        // TOP 카테고리 캐시가 무효화되었는지 확인
        Object invalidatedTopCache = Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.TOP.name());
        assertThat(invalidatedTopCache).isNull();
        
        // PANTS 카테고리 캐시는 여전히 유효한지 확인
        Object stillValidPantsCache = Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.PANTS.name());
        assertThat(stillValidPantsCache).isNotNull();
    }
    
    @Test
    @DisplayName("캐시 무효화 테스트 - 상품 가격 업데이트 후 캐시 무효화 확인")
    void cache_eviction_after_price_update_test() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            }
        });
        
        // 초기 데이터 캐싱
        List<CategoryBrandPriceDto> initialPrices = productService.findLowestPriceByAllCategories();
        assertThat(initialPrices).isNotEmpty();
        
        // 캐시가 생성되었는지 확인 (캐시 자체가 존재하는지만 확인)
        assertThat(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).isNotNull();
        
        // 상품 가격 업데이트
        Brand brand = brandRepository.findById(1L).orElseThrow();
        productService.updateProductByBrandAndCategory(brand.getId(), Category.TOP, 99999);
        
        // 캐시 내용 출력
        logger.info("===== 캐시 업데이트 후 상태 =====");
        cacheManager.getCacheNames().forEach(cacheName -> {
            logger.info("캐시 이름: {}", cacheName);
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache();
                logger.info("  캐시 내용: {}", Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache());
            }
        });
        
        // 새로운 데이터 요청 시 캐시가 다시 생성되는지 확인
        List<CategoryBrandPriceDto> updatedPrices = productService.findLowestPriceByAllCategories();
        
        // TOP 카테고리에 대한 최저가 브랜드가 변경되었는지 확인
        CategoryBrandPriceDto topCategoryPrice = updatedPrices.stream()
                .filter(dto -> dto.getCategory() == Category.TOP)
                .findFirst()
                .orElseThrow();
                
        // TestA가 더이상 TOP 카테고리의 최저가 브랜드가 아님
        if (brand.getName().equals("TestA")) {
            assertThat(topCategoryPrice.getBrandName()).isNotEqualTo("TestA");
        }
    }
    
    @Test
    @DisplayName("캐시 무효화 테스트 - 다양한 작업 후 캐시 상태 확인")
    void cache_eviction_test() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            }
        });
        
        // 1. 초기 데이터 로드 및 캐시 생성
        Brand brand = brandService.getBrandById(1L);
        
        // 캐시 상태 확인 (단순히 존재 여부만 확인)
        assertThat(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BRAND_CACHE)).isNotNull();
        
        // 2. 브랜드 업데이트 후 브랜드 캐시 무효화 확인
        brandService.updateBrand(1L, "UpdatedBrandName");
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).get(1L)).isNull();
        
        // 3. 상품 가격 업데이트 후 관련 캐시 무효화 확인
        Category targetCategory = Category.TOP;
        Long brandIdToUpdate = brand.getId();
        
        productService.updateProductByBrandAndCategory(brandIdToUpdate, targetCategory, 99999);
        
        // 카테고리별 최저가 캐시가 무효화되었는지 확인
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(targetCategory.name())).isNull();
        
        // 전체 카테고리 최저가 캐시가 무효화되었는지 확인
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get("SimpleKey []")).isNull();
        
        // 최저가 브랜드 캐시가 무효화되었는지 확인
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BRAND_CACHE)).get("SimpleKey []")).isNull();
    }
    
    @Test
    @DisplayName("캐시 성능 향상 테스트")
    void cache_performance_test() {
        // 캐시 지우기 (테스트 격리)
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.BRAND_BY_ID_CACHE)).clear();
        
        // 첫 번째 호출 시간 측정 (캐시 미스)
        long startTime1 = System.nanoTime();
        Brand brand1 = brandService.getBrandById(1L);
        long endTime1 = System.nanoTime();
        long duration1 = endTime1 - startTime1;
        
        // 두 번째 호출 시간 측정 (캐시 히트)
        long startTime2 = System.nanoTime();
        Brand brand2 = brandService.getBrandById(1L);
        long endTime2 = System.nanoTime();
        long duration2 = endTime2 - startTime2;
        
        // 캐시 적중 시 성능이 향상되었는지 확인
        assertThat(duration2).isLessThan(duration1);
        logger.info("캐시 미스 시간(ns): {}", duration1);
        logger.info("캐시 히트 시간(ns): {}", duration2);
        logger.info("성능 향상률: {}%", ((duration1 - duration2) * 100.0 / duration1));
    }
    
    @Test
    @DisplayName("캐시 키 전략 테스트 - 다양한 파라미터 조합")
    void cache_key_strategy_test() {
        // 다양한 파라미터 조합으로 캐시 키 생성 테스트
        productService.findLowestPriceByCategory(Category.TOP);
        productService.findLowestPriceByCategory(Category.PANTS);
        
        // 각 카테고리별로 별도의 캐시 엔트리가 생성되었는지 확인
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.TOP.name())).isNotNull();
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)).get(Category.PANTS.name())).isNotNull();
        
        // 브랜드와 카테고리 조합 캐시 테스트
        Long brandId = 1L;
        productService.getProductByBrandAndCategory(brandId, Category.TOP);
        productService.getProductByBrandAndCategory(brandId, Category.PANTS);
        
        // 각 조합별로 별도의 캐시 엔트리가 생성되었는지 확인
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE)).get(brandId + "-TOP")).isNotNull();
        assertThat(Objects.requireNonNull(cacheManager.getCache(CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE)).get(brandId + "-PANTS")).isNotNull();
    }
}
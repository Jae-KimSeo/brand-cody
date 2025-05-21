package org.service.brandcody.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    private Brand testBrand;
    private Long brandId;

    @BeforeEach
    void setUp() {
        testBrand = new Brand();
        testBrand.setName("TestProductBrand");
        testBrand = brandRepository.save(testBrand);
        brandId = testBrand.getId();
    }

    @Test
    @DisplayName("상품 CRUD 전체 흐름 테스트")
    void productCrudLifecycleTest() throws Exception {
        // 새 상품 생성 (POST '/api/products/brand/{brandId}')
        String createRequestJson = "{\"category\":\"TOP\",\"price\":10000}";

        MvcResult createResult = mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value(testBrand.getName()))
                .andExpect(jsonPath("$.category").value(Category.TOP.getDisplayName()))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // 생성된 상품 ID 추출
        String createResponseContent = createResult.getResponse().getContentAsString();
        JsonNode createResponseNode = objectMapper.readTree(createResponseContent);
        Long productId = createResponseNode.get("id").asLong();

        // 데이터베이스에서 상품이 생성되었는지 확인
        Optional<Product> createdProductOpt = productRepository.findById(productId);
        assertThat(createdProductOpt).isPresent();
        assertThat(createdProductOpt.get().getCategory()).isEqualTo(Category.TOP);
        assertThat(createdProductOpt.get().getPrice()).isEqualTo(10000);
        assertThat(createdProductOpt.get().getBrand().getId()).isEqualTo(brandId);

        // 생성된 상품 조회 (GET '/api/products/{id}')
        mockMvc.perform(get("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.brand").value(testBrand.getName()))
                .andExpect(jsonPath("$.category").value(Category.TOP.getDisplayName()))
                .andExpect(jsonPath("$.price").value(10000));

        // 상품 가격 수정 (PUT '/api/products/{id}')
        String updateRequestJson = "{\"category\":\"TOP\",\"price\":15000}";

        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.brand").value(testBrand.getName()))
                .andExpect(jsonPath("$.category").value(Category.TOP.getDisplayName()))
                .andExpect(jsonPath("$.price").value(15000));

        // 데이터베이스에서 상품이 업데이트되었는지 확인
        Optional<Product> updatedProductOpt = productRepository.findById(productId);
        assertThat(updatedProductOpt).isPresent();
        assertThat(updatedProductOpt.get().getPrice()).isEqualTo(15000);
        assertThat(updatedProductOpt.get().getCategory()).isEqualTo(Category.TOP);

        // 브랜드와 카테고리로 상품 가격 수정 (PUT '/api/products/brand/{brandId}/category/{category}')
        String categoryUpdateRequestJson = "{\"category\":\"TOP\",\"price\":20000}";

        mockMvc.perform(put("/api/products/brand/{brandId}/category/{category}", brandId, Category.TOP.getDisplayName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryUpdateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value(testBrand.getName()))
                .andExpect(jsonPath("$.category").value(Category.TOP.getDisplayName()))
                .andExpect(jsonPath("$.price").value(20000));

        // 데이터베이스에서 상품이 업데이트되었는지 확인
        updatedProductOpt = productRepository.findById(productId);
        assertThat(updatedProductOpt).isPresent();
        assertThat(updatedProductOpt.get().getPrice()).isEqualTo(20000);

        // 상품 삭제 (DELETE '/api/products/{id}')
        mockMvc.perform(delete("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 데이터베이스에서 상품이 삭제되었는지 확인
        Optional<Product> deletedProductOpt = productRepository.findById(productId);
        assertThat(deletedProductOpt).isNotPresent();
    }

    @Test
    @DisplayName("동일 브랜드의 동일 카테고리 다중 상품 생성 테스트")
    void createMultipleBrandCategoryProductsTest() throws Exception {
        // Given: 특정 브랜드와 카테고리의 첫 번째 상품 생성
        String firstRequestJson = "{\"category\":\"PANTS\",\"price\":25000}";

        // 첫 번째 상품 생성
        mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequestJson))
                .andExpect(status().isCreated());

        // When: 동일 브랜드와 카테고리로 두 번째 상품 생성 시도
        String secondRequestJson = "{\"category\":\"PANTS\",\"price\":30000}";

        mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequestJson))
                .andExpect(status().isCreated());
                
        // Then: 브랜드-카테고리에 대한 다중 상품이 존재하는지 확인
        long productCount = productRepository.countByBrandIdAndCategory(brandId, Category.PANTS);
        assertThat(productCount).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 시 실패 테스트")
    void getProductByNonExistentIdTest() throws Exception {
        // Given: 존재하지 않는 ID (아주 큰 값)
        Long nonExistentId = 999999L;

        // When & Then: 존재하지 않는 ID로 조회 시 404 응답
        mockMvc.perform(get("/api/products/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 수정 시 실패 테스트")
    void updateProductByNonExistentIdTest() throws Exception {
        // Given: 존재하지 않는 ID와 수정 요청
        Long nonExistentId = 999999L;
        String updateRequestJson = "{\"category\":\"TOP\",\"price\":50000}";

        // When & Then: 존재하지 않는 ID로 수정 시 404 응답
        mockMvc.perform(put("/api/products/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 상품 생성 시 실패 테스트")
    void createProductWithNonExistentBrandIdTest() throws Exception {
        // Given: 존재하지 않는 브랜드 ID와 생성 요청
        Long nonExistentBrandId = 999999L;
        String createRequestJson = "{\"category\":\"HAT\",\"price\":15000}";

        // When & Then: 존재하지 않는 브랜드 ID로 생성 시 404 응답
        mockMvc.perform(post("/api/products/brand/{brandId}", nonExistentBrandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 상품 수정 시 실패 테스트")
    void updateProductWithNonExistentCategoryTest() throws Exception {
        // Given: 존재하지 않는 카테고리
        String nonExistentCategory = "존재하지않는카테고리";
        String updateRequestJson = "{\"category\":\"BAG\",\"price\":40000}";

        // When & Then: 존재하지 않는 카테고리로 수정 시 400 응답
        mockMvc.perform(put("/api/products/brand/{brandId}/category/{category}", brandId, nonExistentCategory)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("음수 가격으로 상품 생성 시 실패 테스트")
    void createProductWithNegativePriceTest() throws Exception {
        // Given: 음수 가격을 가진 생성 요청
        String invalidRequestJson = "{\"category\":\"ACCESSORY\",\"price\":-5000}";

        // When & Then: 음수 가격으로 생성 시 400 응답
        mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }
}
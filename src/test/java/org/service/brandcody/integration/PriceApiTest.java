package org.service.brandcody.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/fixture/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PriceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("전체 카테고리 최저가 상품 조회 API 테스트")
    void lowestPriceForAllCategoriesTest() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/api/products/lowest-price")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String content = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(content);

        // 모든 카테고리가 포함되어 있는지 확인
        JsonNode categoriesNode = rootNode.get("categories");
        assertThat(categoriesNode.isArray()).isTrue();
        assertThat(categoriesNode.size()).isEqualTo(8);

        List<String> categoryNames = new ArrayList<>();

        for (JsonNode categoryNode : categoriesNode) {
            String categoryName = categoryNode.get("category").asText();
            categoryNames.add(categoryName);

            assertThat(categoryNode.has("brandName")).isTrue();
            assertThat(categoryNode.has("price")).isTrue();
            assertThat(categoryNode.has("categoryDisplayName")).isTrue();
            assertThat(categoryNode.has("formattedPrice")).isTrue();

        }

        // 모든 카테고리 타입이 포함되어 있는지 확인
        for (Category category : Category.values()) {
            assertThat(categoryNames).contains(category.name());
        }

        // 총액이 있는지 확인
        assertThat(rootNode.has("totalPrice")).isTrue();
        int totalPrice = rootNode.get("totalPrice").asInt();

        // 총액이 각 카테고리의 합과 일치하는지 확인
        assertThat(totalPrice).isGreaterThan(0);

        // 모든 카테고리 상품 가격의 합과 totalPrice가 일치하는지 확인
        int sumOfPrices = 0;
        for (JsonNode categoryNode : categoriesNode) {
            sumOfPrices += categoryNode.get("price").asInt();
        }
        assertThat(totalPrice).isEqualTo(sumOfPrices);
    }

    @Test
    @DisplayName("단일 브랜드 최저 세트 테스트")
    void singleBrandWithLowestTotalPriceTest() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/api/brands/lowest-price")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String content = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(content);

        // 응답에 브랜드 이름이 포함되어 있는지 확인
        assertThat(rootNode.has("brand")).isTrue();
        String brandName = rootNode.get("brand").asText();

        // 브랜드 이름이 존재하는지 확인
        assertThat(brandName).isNotEmpty();

        // 아이템 목록이 포함되어 있는지 확인
        assertThat(rootNode.has("items")).isTrue();
        JsonNode itemsNode = rootNode.get("items");
        assertThat(itemsNode.isArray()).isTrue();
        assertThat(itemsNode.size()).isEqualTo(8);

        // 카테고리별 필요한 필드가 모두 존재하는지 확인
        for (JsonNode itemNode : itemsNode) {
            assertThat(itemNode.has("category")).isTrue();
            assertThat(itemNode.has("price")).isTrue();
            assertThat(itemNode.has("formattedPrice")).isTrue();
        }

        // 총액 정보가 포함되어 있는지 확인
        assertThat(rootNode.has("totalPrice")).isTrue();
        assertThat(rootNode.has("formattedTotalPrice")).isTrue();
        int totalPrice = rootNode.get("totalPrice").asInt();
        assertThat(totalPrice).isGreaterThan(0);

        // 모든 아이템 가격의 합과 totalPrice가 일치하는지 확인
        int sumOfPrices = 0;
        for (JsonNode itemNode : itemsNode) {
            sumOfPrices += itemNode.get("price").asInt();
        }
        assertThat(totalPrice).isEqualTo(sumOfPrices);
    }

    @Test
    @DisplayName("카테고리별 최고/최저가 비교 테스트 - 정상 케이스")
    void categoryPriceComparisonTest() throws Exception {
        // Given
        String category = "상의";

        // When
        MvcResult result = mockMvc.perform(get("/api/products/category/{category}", category)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(category))
                // 최저가 검증
                .andExpect(jsonPath("$.min").exists())
                .andExpect(jsonPath("$.min.brand").exists())
                .andExpect(jsonPath("$.min.price").exists())
                // 최고가 검증
                .andExpect(jsonPath("$.max").exists())
                .andExpect(jsonPath("$.max.brand").exists())
                .andExpect(jsonPath("$.max.price").exists())
                .andReturn();

        // Then
        String content = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(content);

        // 카테고리 이름 검증
        assertThat(rootNode.get("category").asText()).isEqualTo(category);

        // 최저가 브랜드와 가격 필드 존재 확인
        JsonNode minNode = rootNode.get("min");
        assertThat(minNode.has("brand")).isTrue();
        assertThat(minNode.has("price")).isTrue();
        String minBrand = minNode.get("brand").asText();
        int minPrice = minNode.get("price").asInt();
        assertThat(minBrand).isNotEmpty();
        assertThat(minPrice).isGreaterThan(0);

        // 최고가 브랜드와 가격 필드 존재 확인
        JsonNode maxNode = rootNode.get("max");
        assertThat(maxNode.has("brand")).isTrue();
        assertThat(maxNode.has("price")).isTrue();
        String maxBrand = maxNode.get("brand").asText();
        int maxPrice = maxNode.get("price").asInt();
        assertThat(maxBrand).isNotEmpty();
        assertThat(maxPrice).isGreaterThan(0);

        assertThat(maxPrice).isGreaterThan(minPrice);
    }

    @Test
    @DisplayName("카테고리별 최고/최저가 비교 테스트 - 존재하지 않는 카테고리")
    void categoryPriceComparisonWithInvalidCategoryTest() throws Exception {
        // Given
        String invalidCategory = "존재하지않는카테고리";

        // When & Then
        mockMvc.perform(get("/api/products/category/{category}", invalidCategory)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
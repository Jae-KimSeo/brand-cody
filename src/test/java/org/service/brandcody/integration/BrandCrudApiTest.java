package org.service.brandcody.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.repository.BrandRepository;
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
public class BrandCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    @DisplayName("브랜드 CRUD 전체 흐름 테스트")
    void brandCrudLifecycleTest() throws Exception {
        // 새 브랜드 생성 (POST '/api/brands')
        String createRequestJson = "{\"name\":\"TestBrand\"}";

        MvcResult createResult = mockMvc.perform(post("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TestBrand"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // 생성된 브랜드 ID 추출
        String createResponseContent = createResult.getResponse().getContentAsString();
        JsonNode createResponseNode = objectMapper.readTree(createResponseContent);
        Long brandId = createResponseNode.get("id").asLong();

        // 데이터베이스에서 브랜드가 생성되었는지 확인
        Optional<Brand> createdBrandOpt = brandRepository.findById(brandId);
        assertThat(createdBrandOpt).isPresent();
        assertThat(createdBrandOpt.get().getName()).isEqualTo("TestBrand");

        // 생성된 브랜드 조회 (GET '/api/brands/{id}')
        mockMvc.perform(get("/api/brands/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brandId))
                .andExpect(jsonPath("$.name").value("TestBrand"));

        // 브랜드 정보 수정 (PUT '/api/brands/{id}')
        String updateRequestJson = "{\"name\":\"UpdatedTestBrand\"}";

        mockMvc.perform(put("/api/brands/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brandId))
                .andExpect(jsonPath("$.name").value("UpdatedTestBrand"));

        // 데이터베이스에서 브랜드가 업데이트되었는지 확인
        Optional<Brand> updatedBrandOpt = brandRepository.findById(brandId);
        assertThat(updatedBrandOpt).isPresent();
        assertThat(updatedBrandOpt.get().getName()).isEqualTo("UpdatedTestBrand");

        // 브랜드 삭제 (DELETE '/api/brands/{id}')
        mockMvc.perform(delete("/api/brands/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 데이터베이스에서 브랜드가 삭제되었는지 확인
        Optional<Brand> deletedBrandOpt = brandRepository.findById(brandId);
        assertThat(deletedBrandOpt).isNotPresent();
    }

    @Test
    @DisplayName("중복된 브랜드 이름으로 생성 시 실패 테스트")
    void createDuplicateBrandNameTest() throws Exception {
        // Given: 테스트 브랜드 생성
        String createRequestJson = "{\"name\":\"DuplicateTestBrand\"}";

        // 첫 번째 브랜드 생성
        mockMvc.perform(post("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated());

        // When & Then: 동일한 이름으로 두 번째 브랜드 생성 시도
        mockMvc.perform(post("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 조회 시 실패 테스트")
    void getBrandByNonExistentIdTest() throws Exception {
        // Given: 존재하지 않는 ID (아주 큰 값)
        Long nonExistentId = 999999L;

        // When & Then: 존재하지 않는 ID로 조회 시 404 응답
        mockMvc.perform(get("/api/brands/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 수정 시 실패 테스트")
    void updateBrandByNonExistentIdTest() throws Exception {
        // Given: 존재하지 않는 ID와 수정 요청
        Long nonExistentId = 999999L;
        String updateRequestJson = "{\"name\":\"UpdatedNonExistentBrand\"}";

        // When & Then: 존재하지 않는 ID로 수정 시 404 응답
        mockMvc.perform(put("/api/brands/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 삭제 시 실패 테스트")
    void deleteBrandByNonExistentIdTest() throws Exception {
        // Given: 존재하지 않는 ID
        Long nonExistentId = 999999L;

        // When & Then: 존재하지 않는 ID로 삭제 시 404 응답
        mockMvc.perform(delete("/api/brands/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유효하지 않은 브랜드 이름(빈 문자열)으로 생성 시 실패 테스트")
    void createBrandWithInvalidNameTest() throws Exception {
        // Given: 빈 문자열 이름을 가진 요청
        String invalidRequestJson = "{\"name\":\"\"}";

        // When & Then: 유효하지 않은 이름으로 생성 시 400 응답
        mockMvc.perform(post("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        // Given: null 이름을 가진 요청
        String nullNameRequestJson = "{\"name\":null}";

        // When & Then: null 이름으로 생성 시 400 응답
        mockMvc.perform(post("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullNameRequestJson))
                .andExpect(status().isBadRequest());
    }
}
package org.service.brandcody.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.BrandTotalProjection;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.service.BrandService;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand testBrand;

    @BeforeEach
    void setUp() {
        testBrand = new Brand();
        testBrand.setId(1L);
        testBrand.setName("TestBrand");
    }

    @Test
    @DisplayName("브랜드 ID로 브랜드 조회 - 정상 케이스")
    void getBrandById_ExistingBrand_ReturnsBrand() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));

        // When
        Brand result = brandService.getBrandById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("TestBrand");
        verify(brandRepository).findById(1L);
    }

    @Test
    @DisplayName("브랜드 ID로 브랜드 조회 - 브랜드가 존재하지 않는 경우")
    void getBrandById_NonExistingBrand_ThrowsException() {
        // Given
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> brandService.getBrandById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Brand not found with id: 99");
        verify(brandRepository).findById(99L);
    }

    @Test
    @DisplayName("브랜드 이름으로 브랜드 조회 - 정상 케이스")
    void getBrandByName_ExistingBrand_ReturnsBrand() {
        // Given
        String brandName = "TestBrand";
        when(brandRepository.findByName(brandName)).thenReturn(Optional.of(testBrand));

        // When
        Brand result = brandService.getBrandByName(brandName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(brandName);
        verify(brandRepository).findByName(brandName);
    }

    @Test
    @DisplayName("브랜드 이름으로 브랜드 조회 - 브랜드가 존재하지 않는 경우")
    void getBrandByName_NonExistingBrand_ThrowsException() {
        // Given
        String brandName = "NonExistingBrand";
        when(brandRepository.findByName(brandName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> brandService.getBrandByName(brandName))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Brand not found with name: NonExistingBrand");
        verify(brandRepository).findByName(brandName);
    }

    @Test
    @DisplayName("새 브랜드 생성 - 정상 케이스")
    void createBrand_ValidName_ReturnsCreatedBrand() {
        // Given
        String brandName = "NewBrand";
        Brand newBrand = new Brand();
        newBrand.setId(2L);
        newBrand.setName(brandName);

        when(brandRepository.findByName(brandName)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(newBrand);

        // When
        Brand result = brandService.createBrand(brandName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo(brandName);
        verify(brandRepository).findByName(brandName);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("새 브랜드 생성 - 중복 이름 케이스")
    void createBrand_DuplicateName_ThrowsException() {
        // Given
        String brandName = "TestBrand";
        when(brandRepository.findByName(brandName)).thenReturn(Optional.of(testBrand));

        // When & Then
        assertThatThrownBy(() -> brandService.createBrand(brandName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Brand with name 'TestBrand' already exists");
        verify(brandRepository).findByName(brandName);
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    @DisplayName("브랜드 업데이트 - 정상 케이스")
    void updateBrand_ValidData_ReturnsUpdatedBrand() {
        // Given
        Long brandId = 1L;
        String newName = "UpdatedBrand";
        
        Brand updatedBrand = new Brand();
        updatedBrand.setId(brandId);
        updatedBrand.setName(newName);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.findByName(newName)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);

        // When
        Brand result = brandService.updateBrand(brandId, newName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(brandId);
        assertThat(result.getName()).isEqualTo(newName);
        verify(brandRepository).findById(brandId);
        verify(brandRepository).findByName(newName);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("브랜드 업데이트 - 중복 이름 충돌 케이스")
    void updateBrand_DuplicateName_ThrowsException() {
        // Given
        Long brandId = 1L;
        String newName = "ExistingBrand";
        
        Brand existingBrand = new Brand();
        existingBrand.setId(2L);
        existingBrand.setName(newName);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.findByName(newName)).thenReturn(Optional.of(existingBrand));

        // When & Then
        assertThatThrownBy(() -> brandService.updateBrand(brandId, newName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Brand with name 'ExistingBrand' already exists");
        verify(brandRepository).findById(brandId);
        verify(brandRepository).findByName(newName);
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    @DisplayName("브랜드 삭제 - 정상 케이스")
    void deleteBrand_ExistingBrand_DeletesBrand() {
        // Given
        Long brandId = 1L;
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        doNothing().when(brandRepository).delete(any(Brand.class));

        // When
        brandService.deleteBrand(brandId);

        // Then
        verify(brandRepository).findById(brandId);
        verify(brandRepository).delete(testBrand);
    }

    @Test
    @DisplayName("브랜드 삭제 - 브랜드가 존재하지 않는 경우")
    void deleteBrand_NonExistingBrand_ThrowsException() {
        // Given
        Long brandId = 99L;
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> brandService.deleteBrand(brandId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Brand not found with id: 99");
        verify(brandRepository).findById(brandId);
        verify(brandRepository, never()).delete(any(Brand.class));
    }

    @Test
    @DisplayName("최저가 합계 브랜드 찾기 - 정상 케이스")
    void findBrandWithLowestTotalPrice_ExistingBrands_ReturnsLowestPriceBrand() {
        // Given
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("CheapestBrand");

        BrandTotalProjection projection = BrandTotalProjection.of(brand, 31700);
        List<BrandTotalProjection> projections = List.of(projection);

        when(brandRepository.findBrandWithLowestTotalPrice(any(PageRequest.class))).thenReturn(projections);

        // When
        BrandTotalProjection result = brandService.findBrandWithLowestTotalPrice();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.brand().getName()).isEqualTo("CheapestBrand");
        assertThat(result.totalPrice()).isEqualTo(31700);
        verify(brandRepository).findBrandWithLowestTotalPrice(any(PageRequest.class));
    }

    @Test
    @DisplayName("최저가 합계 브랜드 찾기 - 상품이 부족한 경우")
    void findBrandWithLowestTotalPrice_InsufficientProducts_ThrowsException() {
        // Given
        when(brandRepository.findBrandWithLowestTotalPrice(any(PageRequest.class))).thenReturn(new ArrayList<>());

        // When & Then
        assertThatThrownBy(() -> brandService.findBrandWithLowestTotalPrice())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("모든 카테고리의 상품을 보유한 브랜드를 찾을 수 없습니다");
        verify(brandRepository).findBrandWithLowestTotalPrice(any(PageRequest.class));
    }

    @Test
    @DisplayName("모든 브랜드의 총액 조회")
    void findAllBrandsWithTotalPrice_ReturnsAllBrands() {
        // Given
        Brand brand1 = new Brand();
        brand1.setId(1L);
        brand1.setName("Brand1");

        Brand brand2 = new Brand();
        brand2.setId(2L);
        brand2.setName("Brand2");

        List<BrandTotalProjection> projections = Arrays.asList(
                BrandTotalProjection.of(brand1, 35000),
                BrandTotalProjection.of(brand2, 37000)
        );

        when(brandRepository.findAllBrandsWithTotalPrice()).thenReturn(projections);

        // When
        List<BrandTotalProjection> results = brandService.findAllBrandsWithTotalPrice();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).brand().getName()).isEqualTo("Brand1");
        assertThat(results.get(0).totalPrice()).isEqualTo(35000);
        assertThat(results.get(1).brand().getName()).isEqualTo("Brand2");
        assertThat(results.get(1).totalPrice()).isEqualTo(37000);
        verify(brandRepository).findAllBrandsWithTotalPrice();
    }
}
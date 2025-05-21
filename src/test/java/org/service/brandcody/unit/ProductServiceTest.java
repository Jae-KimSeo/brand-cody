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
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.service.brandcody.service.ProductService;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductService productService;

    private Brand testBrand;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testBrand = new Brand();
        testBrand.setId(1L);
        testBrand.setName("TestBrand");

        testProduct = new Product(Category.TOP, 10000);
        testProduct.setId(1L);
        testProduct.setBrand(testBrand);
        testBrand.addProduct(testProduct);
    }

    @Test
    @DisplayName("상품 ID로 상품 조회 - 정상 케이스")
    void getProductById_ExistingProduct_ReturnsProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategory()).isEqualTo(Category.TOP);
        assertThat(result.getPrice()).isEqualTo(10000);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("상품 ID로 상품 조회 - 상품이 존재하지 않는 경우")
    void getProductById_NonExistingProduct_ThrowsException() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Product not found with id: 99");
        verify(productRepository).findById(99L);
    }

    @Test
    @DisplayName("새 상품 생성 - 정상 케이스")
    void createProduct_ValidData_ReturnsCreatedProduct() {
        // Given
        Long brandId = 1L;
        Category category = Category.PANTS;
        Integer price = 5000;

        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("TestBrand");

        Product newProduct = new Product(category, price);
        newProduct.setBrand(brand);
        newProduct.setId(2L);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(productRepository.findByBrandIdAndCategory(brandId, category)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // When
        Product result = productService.createProduct(brandId, category, price);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getPrice()).isEqualTo(price);
        assertThat(result.getBrand()).isEqualTo(brand);
        verify(brandRepository).findById(brandId);
        verify(productRepository).findByBrandIdAndCategory(brandId, category);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("새 상품 생성 - 브랜드-카테고리 중복 케이스")
    void createProduct_DuplicateBrandCategory_ThrowsException() {
        // Given
        Long brandId = 1L;
        Category category = Category.TOP;
        Integer price = 5000;

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(productRepository.findByBrandIdAndCategory(brandId, category)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(brandId, category, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product already exists for this brand and category");
        verify(brandRepository).findById(brandId);
        verify(productRepository).findByBrandIdAndCategory(brandId, category);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("새 상품 생성 - 브랜드가 없는 경우")
    void createProduct_BrandNotFound_ThrowsException() {
        // Given
        Long brandId = 99L;
        Category category = Category.TOP;
        Integer price = 5000;

        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(brandId, category, price))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Brand not found with id: 99");
        verify(brandRepository).findById(brandId);
        verify(productRepository, never()).findByBrandIdAndCategory(any(), any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 가격 업데이트 - 정상 케이스")
    void updateProduct_ExistingProduct_ReturnsUpdatedProduct() {
        // Given
        Long productId = 1L;
        Integer newPrice = 15000;

        Product updatedProduct = new Product(Category.TOP, newPrice);
        updatedProduct.setId(productId);
        updatedProduct.setBrand(testBrand);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateProduct(productId, newPrice);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getPrice()).isEqualTo(newPrice);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 가격 업데이트 - 상품이 존재하지 않는 경우")
    void updateProduct_NonExistingProduct_ThrowsException() {
        // Given
        Long productId = 99L;
        Integer newPrice = 15000;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, newPrice))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Product not found with id: 99");
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("브랜드와 카테고리로 상품 가격 업데이트 - 정상 케이스")
    void updateProductByBrandAndCategory_ExistingProduct_ReturnsUpdatedProduct() {
        // Given
        Long brandId = 1L;
        Category category = Category.TOP;
        Integer newPrice = 15000;

        Product updatedProduct = new Product(category, newPrice);
        updatedProduct.setId(1L);
        updatedProduct.setBrand(testBrand);

        when(productRepository.findByBrandIdAndCategory(brandId, category)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateProductByBrandAndCategory(brandId, category, newPrice);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getPrice()).isEqualTo(newPrice);
        verify(productRepository).findByBrandIdAndCategory(brandId, category);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("브랜드와 카테고리로 상품 가격 업데이트 - 상품이 존재하지 않는 경우")
    void updateProductByBrandAndCategory_NonExistingProduct_ThrowsException() {
        // Given
        Long brandId = 1L;
        Category category = Category.PANTS;
        Integer newPrice = 15000;

        when(productRepository.findByBrandIdAndCategory(brandId, category)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProductByBrandAndCategory(brandId, category, newPrice))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Product not found for brand id: 1 and category: PANTS");
        verify(productRepository).findByBrandIdAndCategory(brandId, category);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 - 정상 케이스")
    void deleteProduct_ExistingProduct_DeletesProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).delete(testProduct);
    }

    @Test
    @DisplayName("상품 삭제 - 상품이 존재하지 않는 경우")
    void deleteProduct_NonExistingProduct_ThrowsException() {
        // Given
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Product not found with id: 99");
        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("8개 카테고리의 각각 다른 브랜드 최저가 합산")
    void calculateTotalLowestPriceAcrossCategories_ReturnsCorrectSum() {
        // Given
        List<CategoryBrandPriceDto> lowestPriceItems = Arrays.asList(
                new CategoryBrandPriceDto(Category.TOP, "BrandA", 9000),
                new CategoryBrandPriceDto(Category.OUTER, "BrandB", 4800),
                new CategoryBrandPriceDto(Category.PANTS, "BrandC", 3000),
                new CategoryBrandPriceDto(Category.SNEAKERS, "BrandD", 8500),
                new CategoryBrandPriceDto(Category.BAG, "BrandE", 1800),
                new CategoryBrandPriceDto(Category.HAT, "BrandF", 1400),
                new CategoryBrandPriceDto(Category.SOCKS, "BrandG", 1500),
                new CategoryBrandPriceDto(Category.ACCESSORY, "BrandH", 1700)
        );

        when(productRepository.findLowestPriceByCategory()).thenReturn(lowestPriceItems);

        // When
        int result = productService.calculateTotalLowestPriceAcrossCategories();

        // Then
        int expectedTotal = 9000 + 4800 + 3000 + 8500 + 1800 + 1400 + 1500 + 1700; // 31700
        assertThat(result).isEqualTo(expectedTotal);
        verify(productRepository).findLowestPriceByCategory();
    }
}
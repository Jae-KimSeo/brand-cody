package org.service.brandcody.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.brandcody.config.CacheConfig;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Cacheable(CacheConfig.PRODUCT_CACHE)
    public List<Product> getAllProducts() {
        log.debug("Fetching all products from database");
        return productRepository.findAll();
    }

    @Cacheable(value = CacheConfig.PRODUCT_BY_ID_CACHE, key = "#id")
    public Product getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Cacheable(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, key = "#brandId")
    public List<Product> getProductsByBrand(Long brandId) {
        log.debug("Fetching products for brand id: {}", brandId);
        return productRepository.findByBrandIdOrderByCategory(brandId);
    }

    @Cacheable(value = CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE, key = "#brandId + '-' + #category.name()")
    public Optional<Product> getProductByBrandAndCategory(Long brandId, Category category) {
        log.debug("Fetching product for brand id: {} and category: {}", brandId, category);
        return productRepository.findByBrandIdAndCategory(brandId, category);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, key = "#brandId"),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE, key = "#brandId + '-' + #category.name()"),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.HIGHEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public Product createProduct(Long brandId, Category category, Integer price) {
        log.debug("Creating new product for brand id: {} and category: {} with price: {}", brandId, category, price);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new NoSuchElementException("Brand not found with id: " + brandId));

        try {
            Product product = new Product(category, price);
            brand.addProduct(product);
            return productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent product creation detected for brand {} and category {}", brandId, category);
            throw new IllegalArgumentException("Product already exists for this brand and category");
        }
    }

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_ID_CACHE, key = "#id"),
        @CacheEvict(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.HIGHEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public Product updateProduct(Long id, Integer price) {
        log.debug("Attempting to update product with id: {} to price: {}", id, price);
        Product product = getProductById(id);
        product.updatePrice(price);
        try {
            return productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure when updating product id: {}. Retry attempt will follow.", id);
            throw e;
        }
    }

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, key = "#brandId"),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE, key = "#brandId + '-' + #category.name()"),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE, key = "#category.name()"),
        @CacheEvict(value = CacheConfig.HIGHEST_PRICE_BY_CATEGORY_CACHE, key = "#category.name()"),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public Product updateProductByBrandAndCategory(Long brandId, Category category, Integer price) {
        log.debug("Attempting to update product for brand id: {} and category: {} to price: {}", brandId, category, price);
        Product product = productRepository.findByBrandIdAndCategory(brandId, category)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found for brand id: " + brandId + " and category: " + category));
        
        product.updatePrice(price);
        try {
            return productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure when updating product for brand id: {} and category: {}. Retry attempt will follow.", 
                     brandId, category);
            throw e;
        }
    }

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_ID_CACHE, key = "#id"),
        @CacheEvict(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCT_BY_BRAND_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.HIGHEST_PRICE_BY_CATEGORY_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public void deleteProduct(Long id) {
        log.debug("Attempting to delete product with id: {}", id);
        Product product = getProductById(id);
        Brand brand = product.getBrand();
        brand.removeProduct(product);
        try {
            productRepository.delete(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure when deleting product id: {}. Retry attempt will follow.", id);
            throw e;
        }
    }

    @Cacheable(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)
    public List<CategoryBrandPriceDto> findLowestPriceByAllCategories() {
        log.debug("Calculating lowest price for all categories");
        return productRepository.findLowestPriceByCategory();
    }

    @Cacheable(value = CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE, key = "#category.name()")
    public List<CategoryBrandPriceDto> findLowestPriceByCategory(Category category) {
        log.debug("Calculating lowest price for category: {}", category);
        return productRepository.findLowestPriceByCategory(category);
    }

    @Cacheable(value = CacheConfig.HIGHEST_PRICE_BY_CATEGORY_CACHE, key = "#category.name()")
    public List<CategoryBrandPriceDto> findHighestPriceByCategory(Category category) {
        log.debug("Calculating highest price for category: {}", category);
        return productRepository.findHighestPriceByCategory(category);
    }

    @Cacheable(CacheConfig.LOWEST_PRICE_BY_CATEGORY_CACHE)
    public int calculateTotalLowestPriceAcrossCategories() {
        log.debug("Calculating total lowest price across all categories");
        List<CategoryBrandPriceDto> lowestPriceItems = findLowestPriceByAllCategories();
        return lowestPriceItems.stream()
                .mapToInt(CategoryBrandPriceDto::getPrice)
                .sum();
    }
}
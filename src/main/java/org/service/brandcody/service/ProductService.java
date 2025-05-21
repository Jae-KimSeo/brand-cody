package org.service.brandcody.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
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

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    public List<Product> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandIdOrderByCategory(brandId);
    }

    public Optional<Product> getProductByBrandAndCategory(Long brandId, Category category) {
        return productRepository.findByBrandIdAndCategory(brandId, category);
    }

    @Transactional
    public Product createProduct(Long brandId, Category category, Integer price) {
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

    public List<CategoryBrandPriceDto> findLowestPriceByAllCategories() {
        return productRepository.findLowestPriceByCategory();
    }

    public List<CategoryBrandPriceDto> findLowestPriceByCategory(Category category) {
        return productRepository.findLowestPriceByCategory(category);
    }

    public List<CategoryBrandPriceDto> findHighestPriceByCategory(Category category) {
        return productRepository.findHighestPriceByCategory(category);
    }

    public int calculateTotalLowestPriceAcrossCategories() {
        List<CategoryBrandPriceDto> lowestPriceItems = findLowestPriceByAllCategories();
        return lowestPriceItems.stream()
                .mapToInt(CategoryBrandPriceDto::getPrice)
                .sum();
    }
}
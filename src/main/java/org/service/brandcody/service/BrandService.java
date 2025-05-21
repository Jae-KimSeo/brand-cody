package org.service.brandcody.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.brandcody.config.CacheConfig;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.dto.BrandTotalProjection;
import org.service.brandcody.repository.BrandRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
    private final BrandRepository brandRepository;

    @Cacheable(CacheConfig.BRAND_CACHE)
    public List<Brand> getAllBrands() {
        log.debug("Fetching all brands from database");
        return brandRepository.findAll();
    }

    @Cacheable(value = CacheConfig.BRAND_BY_ID_CACHE, key = "#id")
    public Brand getBrandById(Long id) {
        log.debug("Fetching brand with id: {}", id);
        return brandRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Brand not found with id: " + id));
    }

    @Cacheable(value = CacheConfig.BRAND_BY_NAME_CACHE, key = "#name")
    public Brand getBrandByName(String name) {
        log.debug("Fetching brand with name: {}", name);
        return brandRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Brand not found with name: " + name));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public Brand createBrand(String name) {
        log.debug("Creating new brand with name: {}", name);
        try {
            Brand brand = new Brand();
            brand.setName(name);
            return brandRepository.save(brand);
        } catch (DataIntegrityViolationException e) {
            log.warn("Attempt to create duplicate brand with name: {}", name);
            throw new IllegalArgumentException("Brand with name '" + name + "' already exists");
        }
    }

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.BRAND_BY_ID_CACHE, key = "#id"),
        @CacheEvict(value = CacheConfig.BRAND_BY_NAME_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true)
    })
    public Brand updateBrand(Long id, String name) {
        log.debug("Attempting to update brand with id: {} to name: {}", id, name);
        Brand brand = getBrandById(id);
        
        try {
            brand.setName(name);
            return brandRepository.save(brand);
        } catch (DataIntegrityViolationException e) {
            log.warn("Attempt to update brand to a duplicate name: {}", name);
            throw new IllegalArgumentException("Brand with name '" + name + "' already exists");
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure when updating brand id: {}. Retry attempt will follow.", id);
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
        @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.BRAND_BY_ID_CACHE, key = "#id"),
        @CacheEvict(value = CacheConfig.BRAND_BY_NAME_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.LOWEST_PRICE_BRAND_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.PRODUCTS_BY_BRAND_CACHE, key = "#id")
    })
    public void deleteBrand(Long id) {
        log.debug("Attempting to delete brand with id: {}", id);
        Brand brand = getBrandById(id);
        try {
            brandRepository.delete(brand);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure when deleting brand id: {}. Retry attempt will follow.", id);
            throw e;
        }
    }

    @Cacheable(CacheConfig.LOWEST_PRICE_BRAND_CACHE)
    public BrandTotalProjection findBrandWithLowestTotalPrice() {
        log.debug("Calculating brand with lowest total price");
        List<BrandTotalProjection> result = brandRepository.findBrandWithLowestTotalPrice(PageRequest.of(0, 1));
        
        if (result.isEmpty()) {
            throw new NoSuchElementException("모든 카테고리의 상품을 보유한 브랜드를 찾을 수 없습니다. 각 브랜드는 모든 카테고리(상의, 아우터, 바지, 스니커즈, 가방, 모자, 양말, 액세서리)의 상품을 가지고 있어야 합니다.");
        }
        
        return result.getFirst();
    }

    @Cacheable(CacheConfig.BRAND_CACHE)
    public List<BrandTotalProjection> findAllBrandsWithTotalPrice() {
        log.debug("Fetching all brands with total price");
        return brandRepository.findAllBrandsWithTotalPrice();
    }
}
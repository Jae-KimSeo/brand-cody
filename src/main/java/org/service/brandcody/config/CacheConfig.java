package org.service.brandcody.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String BRAND_CACHE = "brandCache";
    public static final String BRAND_BY_ID_CACHE = "brandByIdCache";
    public static final String BRAND_BY_NAME_CACHE = "brandByNameCache";
    public static final String PRODUCT_CACHE = "productCache";
    public static final String PRODUCT_BY_ID_CACHE = "productByIdCache";
    public static final String PRODUCTS_BY_BRAND_CACHE = "productsByBrandCache";
    public static final String PRODUCT_BY_BRAND_CATEGORY_CACHE = "productByBrandCategoryCache";
    public static final String LOWEST_PRICE_BY_CATEGORY_CACHE = "lowestPriceByCategoryCache";
    public static final String HIGHEST_PRICE_BY_CATEGORY_CACHE = "highestPriceByCategoryCache";
    public static final String LOWEST_PRICE_BRAND_CACHE = "lowestPriceBrandCache";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCacheNames(Arrays.asList(
            BRAND_CACHE, 
            BRAND_BY_ID_CACHE, 
            BRAND_BY_NAME_CACHE,
            PRODUCT_CACHE, 
            PRODUCT_BY_ID_CACHE, 
            PRODUCTS_BY_BRAND_CACHE,
            PRODUCT_BY_BRAND_CATEGORY_CACHE,
            LOWEST_PRICE_BY_CATEGORY_CACHE,
            HIGHEST_PRICE_BY_CATEGORY_CACHE,
            LOWEST_PRICE_BRAND_CACHE
        ));

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats());
        
        return cacheManager;
    }
}
package org.service.brandcody.service;

import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.dto.CategoryBrandPriceDto;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

        Optional<Product> existingProduct = productRepository.findByBrandIdAndCategory(brandId, category);
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product already exists for this brand and category");
        }

        Product product = new Product(category, price);
        brand.addProduct(product);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Integer price) {
        Product product = getProductById(id);
        product.updatePrice(price);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProductByBrandAndCategory(Long brandId, Category category, Integer price) {
        Product product = productRepository.findByBrandIdAndCategory(brandId, category)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found for brand id: " + brandId + " and category: " + category));
        
        product.updatePrice(price);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        Brand brand = product.getBrand();
        brand.removeProduct(product);
        productRepository.delete(product);
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
package org.service.brandcody.service;

import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
    private final BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Brand not found with id: " + id));
    }

    public Brand getBrandByName(String name) {
        return brandRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Brand not found with name: " + name));
    }

    @Transactional
    public Brand createBrand(String name) {
        if (brandRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Brand with name '" + name + "' already exists");
        }
        
        Brand brand = new Brand();
        brand.setName(name);
        return brandRepository.save(brand);
    }

    @Transactional
    public Brand updateBrand(Long id, String name) {
        Brand brand = getBrandById(id);
        
        if (!brand.getName().equals(name) && brandRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Brand with name '" + name + "' already exists");
        }
        
        brand.setName(name);
        return brandRepository.save(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = getBrandById(id);
        brandRepository.delete(brand);
    }

    public Map<String, Object> findBrandWithLowestTotalPrice() {
        Object[] result = brandRepository.findBrandWithLowestTotalPrice();
        if (result == null || result.length < 2) {
            throw new NoSuchElementException("No brand found with products in all categories");
        }
        
        Brand brand = (Brand) result[0];
        Integer totalPrice = ((Number) result[1]).intValue();
        
        Map<String, Object> response = new HashMap<>();
        response.put("brand", brand);
        response.put("totalPrice", totalPrice);
        
        return response;
    }
    
    public List<Object> findAllBrandsWithTotalPrice() {
        return brandRepository.findAllBrandsWithTotalPrice();
    }
}
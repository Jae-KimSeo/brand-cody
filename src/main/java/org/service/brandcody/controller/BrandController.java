package org.service.brandcody.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.dto.BrandTotalProjection;
import org.service.brandcody.dto.request.BrandRequest;
import org.service.brandcody.dto.response.BrandResponse;
import org.service.brandcody.dto.response.SingleBrandResponse;
import org.service.brandcody.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands().stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(BrandResponse.from(brand));
    }

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        Brand brand = brandService.createBrand(request.getName());
        return new ResponseEntity<>(BrandResponse.from(brand), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        Brand brand = brandService.updateBrand(id, request.getName());
        return ResponseEntity.ok(BrandResponse.from(brand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lowest-price")
    public ResponseEntity<SingleBrandResponse> getBrandWithLowestTotalPrice() {
        BrandTotalProjection projection = brandService.findBrandWithLowestTotalPrice();
        return ResponseEntity.ok(projection.toResponse());
    }
}
package org.service.brandcody.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.dto.BrandTotalProjection;
import org.service.brandcody.dto.request.BrandRequest;
import org.service.brandcody.dto.response.BrandResponse;
import org.service.brandcody.dto.response.SingleBrandResponse;
import org.service.brandcody.exception.ErrorResponse;
import org.service.brandcody.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
@Tag(name = "브랜드 API", description = "브랜드 정보 조회 및 관리를 위한 API")
public class BrandController {
    private final BrandService brandService;

    @Operation(summary = "모든 브랜드 조회", description = "시스템에 등록된 모든 브랜드 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands().stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(brands);
    }

    @Operation(summary = "브랜드 상세 조회", description = "특정 ID의 브랜드 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "브랜드 조회 성공"),
            @ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long id) {
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(BrandResponse.from(brand));
    }

    @Operation(summary = "새 브랜드 등록", description = "새로운 브랜드를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "브랜드 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 브랜드", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(
            @Parameter(description = "브랜드 정보", required = true) @Valid @RequestBody BrandRequest request) {
        Brand brand = brandService.createBrand(request.getName());
        return new ResponseEntity<>(BrandResponse.from(brand), HttpStatus.CREATED);
    }

    @Operation(summary = "브랜드 정보 수정", description = "기존 브랜드의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "브랜드 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 브랜드 이름", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정할 브랜드 정보", required = true) @Valid @RequestBody BrandRequest request) {
        Brand brand = brandService.updateBrand(id, request.getName());
        return ResponseEntity.ok(BrandResponse.from(brand));
    }

    @Operation(summary = "브랜드 삭제", description = "시스템에서 특정 브랜드를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "브랜드 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "최저가 브랜드 조회", 
            description = "단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격인 브랜드와 총액을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최저가 브랜드 조회 성공"),
            @ApiResponse(responseCode = "404", description = "조건에 맞는 브랜드가 없음", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/lowest-price")
    public ResponseEntity<SingleBrandResponse> getBrandWithLowestTotalPrice() {
        BrandTotalProjection projection = brandService.findBrandWithLowestTotalPrice();
        return ResponseEntity.ok(projection.toResponse());
    }
}
package org.service.brandcody.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.service.brandcody.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
public class ConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

    private Brand testBrand;
    private Long brandId;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 삭제
        productRepository.deleteAll();
        brandRepository.deleteAll();

        // 테스트 브랜드 생성
        testBrand = new Brand();
        testBrand.setName("ConcurrencyTestBrand");
        testBrand = brandRepository.save(testBrand);
        brandId = testBrand.getId();
    }

    @Test
    @DisplayName("동일 브랜드-카테고리로 상품 동시 생성 시 하나만 성공해야 함")
    void concurrentProductCreationTest() throws Exception {
        // 동일한 카테고리와 브랜드로 동시에 두 개의 상품을 생성
        String productRequestJson = "{\"category\":\"TOP\",\"price\":10000}";
        
        // 성공/실패 카운터
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);
        
        // 스레드 동기화를 위한 래치
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(2);
        
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        
        // 첫 번째 스레드: 상품 생성 요청
        executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                MvcResult result = mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestJson))
                        .andReturn();
                
                int status = result.getResponse().getStatus();
                if (status == 201) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 번째 스레드: 동일한 상품 생성 요청
        executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                MvcResult result = mockMvc.perform(post("/api/products/brand/{brandId}", brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestJson))
                        .andReturn();
                
                int status = result.getResponse().getStatus();
                if (status == 201) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 스레드를 동시에 시작
        startLatch.countDown();
        
        // 모든 스레드가 완료될 때까지 대기 (최대 10초)
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // 1. 모든 스레드가 제한 시간 내에 종료되었는지 확인
        assertThat(completed).as("All threads should complete within the timeout").isTrue();
        
        // 2. 데이터베이스 상태 검증: 해당 브랜드-카테고리 조합의 상품이 정확히 하나만 존재해야 함
        long productCount = productRepository.countByBrandIdAndCategory(brandId, Category.TOP);
        assertThat(productCount).as("Only one product should exist for the brand-category combination").isEqualTo(1);
        
        // 3. 검증: 요청이 정확히 2개 처리됨 (성공 1개 + 실패 1개)
        int total = successCount.get() + failureCount.get();
        assertThat(total).as("Total requests processed should be 2").isEqualTo(2);
        
        // 4. 성공 1개, 실패 1개 검증
        assertThat(successCount.get()).as("Successfully created products should be 1").isEqualTo(1);
        assertThat(failureCount.get()).as("Failed creation attempts should be 1").isEqualTo(1);
    }
    
    @Test
    @DisplayName("낙관적 락을 통한 동시 수정 충돌 테스트 및 재시도 확인")
    void optimisticLockingAndRetryTest() throws Exception {
        // 상품 생성
        Product product = productService.createProduct(brandId, Category.TOP, 10000);
        Long productId = product.getId();
        
        // 업데이트 요청 JSON
        String updateRequest1 = "{\"price\":15000}";
        String updateRequest2 = "{\"price\":20000}";
        
        // 스레드 동기화를 위한 래치
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(2);
        
        // 성공/실패 카운터
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        
        // 첫 번째 스레드: 가격 업데이트 1
        Future<?> future1 = executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                Thread.sleep(10);
                
                MvcResult result = mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest1))
                        .andReturn();
                
                int status = result.getResponse().getStatus();
                if (status == 200) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 번째 스레드: 가격 업데이트 2
        Future<?> future2 = executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                
                MvcResult result = mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest2))
                        .andReturn();
                
                int status = result.getResponse().getStatus();
                if (status == 200) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 스레드를 동시에 시작
        startLatch.countDown();
        
        // 모든 스레드가 완료될 때까지 대기 (최대 10초)
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // 두 작업이 모두 완료될 때까지 대기
        future1.get(5, TimeUnit.SECONDS);
        future2.get(5, TimeUnit.SECONDS);
        
        // 검증
        // 1. 모든 스레드가 제한 시간 내에 종료되었는지 확인
        assertThat(completed).as("All threads should complete within the timeout").isTrue();
        
        // 2. 두 요청 모두 성공했는지 확인 (재시도를 통해)
        assertThat(successCount.get()).as("Both update operations should succeed due to retry").isEqualTo(2);
        assertThat(failureCount.get()).as("No failures should occur with retry").isEqualTo(0);
        
        // 3. 최신 상품 정보 확인
        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        // 둘 중 하나의 가격이 적용되어 있어야 함
        assertThat(updatedProduct.getPrice()).as("Product price should be updated to one of the values")
                .isIn(15000, 20000);
    }
}
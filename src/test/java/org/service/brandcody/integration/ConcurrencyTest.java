package org.service.brandcody.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.service.brandcody.domain.Brand;
import org.service.brandcody.domain.Category;
import org.service.brandcody.domain.Product;
import org.service.brandcody.repository.BrandRepository;
import org.service.brandcody.repository.ProductRepository;
import org.service.brandcody.service.BrandService;
import org.service.brandcody.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    
    @Autowired
    private BrandService brandService;

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
        assertThat(completed).as("모든 스레드가 타임아웃 시간 내에 완료되어야 함").isTrue();
        
        // 2. 데이터베이스 상태 검증: 해당 브랜드-카테고리 조합의 상품이 정확히 하나만 존재해야 함
        long productCount = productRepository.countByBrandIdAndCategory(brandId, Category.TOP);
        assertThat(productCount).as("브랜드-카테고리 조합에 대해 하나의 상품만 존재해야 함").isEqualTo(1);
        
        // 3. 검증: 요청이 정확히 2개 처리됨 (성공 1개 + 실패 1개)
        int total = successCount.get() + failureCount.get();
        assertThat(total).as("총 처리된 요청은 2개여야 함").isEqualTo(2);
        
        // 4. 성공 1개, 실패 1개 검증
        assertThat(successCount.get()).as("성공적으로 생성된 상품은 1개여야 함").isEqualTo(1);
        assertThat(failureCount.get()).as("실패한 생성 시도는 1개여야 함").isEqualTo(1);
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
        assertThat(completed).as("모든 스레드가 타임아웃 시간 내에 완료되어야 함").isTrue();
        
        // 2. 두 요청 모두 성공했는지 확인 (재시도를 통해)
        assertThat(successCount.get()).as("재시도 덕분에 두 업데이트 작업 모두 성공해야 함").isEqualTo(2);
        assertThat(failureCount.get()).as("재시도로 인해 실패가 발생하지 않아야 함").isEqualTo(0);
        
        // 3. 최신 상품 정보 확인
        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        // 둘 중 하나의 가격이 적용되어 있어야 함
        assertThat(updatedProduct.getPrice()).as("상품 가격이 두 값 중 하나로 업데이트되어야 함")
                .isIn(15000, 20000);
    }
    
    @Test
    @DisplayName("서비스 계층에서 브랜드 동시 수정 낙관적 락 테스트")
    void brandServiceOptimisticLockingTest() throws Exception {
        // 테스트 데이터 준비
        final String initialName = "ServiceConcurrencyTest";
        final String newName1 = "UpdatedBrand1";
        final String newName2 = "UpdatedBrand2";
        
        // 브랜드 생성
        Brand brand = brandService.createBrand(initialName);
        Long brandId = brand.getId();
        
        // 스레드 동기화를 위한 래치
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(2);
        
        // 결과 추적을 위한 Atomic 변수들
        final AtomicBoolean firstThreadSuccess = new AtomicBoolean(false);
        final AtomicBoolean secondThreadSuccess = new AtomicBoolean(false);
        final AtomicReference<Exception> firstThreadException = new AtomicReference<>();
        final AtomicReference<Exception> secondThreadException = new AtomicReference<>();
        
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        
        // 첫 번째 스레드: 브랜드명 업데이트 1
        executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                Thread.sleep(10); // 두 번째 스레드보다 약간 늦게 시작
                
                Brand updatedBrand = brandService.updateBrand(brandId, newName1);
                firstThreadSuccess.set(true);
            } catch (Exception e) {
                firstThreadException.set(e);
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 번째 스레드: 브랜드명 업데이트 2
        executorService.submit(() -> {
            try {
                startLatch.await(); // 메인 스레드가 시작 신호를 줄 때까지 대기
                
                Brand updatedBrand = brandService.updateBrand(brandId, newName2);
                secondThreadSuccess.set(true);
            } catch (Exception e) {
                secondThreadException.set(e);
            } finally {
                endLatch.countDown();
            }
        });
        
        // 두 스레드를 동시에 시작
        startLatch.countDown();
        
        // 모든 스레드가 완료될 때까지 대기 (최대 10초)
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // 검증
        // 1. 모든 스레드가 제한 시간 내에 종료되었는지 확인
        assertThat(completed).as("모든 스레드가 타임아웃 시간 내에 완료되어야 함").isTrue();
        
        // 2. 두 스레드 중 하나만 성공해야 함 (낙관적 락 + 재시도)
        assertThat(firstThreadSuccess.get() || secondThreadSuccess.get())
            .as("적어도 하나의 스레드가 성공해야 함")
            .isTrue();
        
        // 3. 최종 브랜드명 확인
        Brand finalBrand = brandRepository.findById(brandId).orElseThrow();
        assertThat(finalBrand.getName()).as("브랜드 이름이 새로운 이름 중 하나로 업데이트되어야 함")
            .isIn(newName1, newName2);
    }
    
    @Test
    @DisplayName("서비스 메소드에서 낙관적 락 예외 발생 및 처리 테스트")
    void optimisticLockingExceptionHandlingTest() throws Exception {
        // 상품 생성
        Product product = productService.createProduct(brandId, Category.PANTS, 10000);
        Long productId = product.getId();
        
        // 첫 번째 트랜잭션에서 상품 조회 (버전 로드)
        Product product1 = productRepository.findById(productId).orElseThrow();
        
        // 두 번째 트랜잭션에서 상품 수정 (버전 증가)
        Product product2 = productService.updateProduct(productId, 15000);
        
        // 첫 번째 트랜잭션의 엔티티로 수정 시도 (이미 버전이 증가되어 있으므로 실패해야 함)
        // 이 테스트를 위해 재시도가 없는 직접적인 저장 작업 수행
        product1.updatePrice(20000);
        
        // 낙관적 락 예외가 발생해야 함
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            productRepository.saveAndFlush(product1);
        });
        
        // 최종 상태 확인 - 두 번째 트랜잭션의 가격이 유지되어야 함
        Product finalProduct = productRepository.findById(productId).orElseThrow();
        assertThat(finalProduct.getPrice()).as("성공한 트랜잭션의 가격인 15000이 유지되어야 함")
            .isEqualTo(15000);
        assertThat(finalProduct.getVersion()).as("버전이 증가되어야 함")
            .isEqualTo(1);
    }
    
    @Test
    @DisplayName("다중 사용자가 동시에 최저가 조회 및 업데이트하는 시나리오 테스트")
    void multipleUsersPerformingConcurrentOperationsTest() throws Exception {
        // 테스트 준비: 여러 브랜드와 상품 생성
        final int brandCount = 3;
        final int threadCount = 10;
        final List<Long> brandIds = new ArrayList<>();
        final List<Long> productIds = new ArrayList<>();
        
        // 테스트 브랜드 및 상품 생성
        for (int i = 0; i < brandCount; i++) {
            Brand brand = brandService.createBrand("ConcurrentBrand" + i);
            brandIds.add(brand.getId());
            
            // 각 브랜드에 여러 카테고리의 상품 추가
            for (Category category : Category.values()) {
                int price = 10000 + (i * 1000) + (category.ordinal() * 100);
                Product product = productService.createProduct(brand.getId(), category, price);
                productIds.add(product.getId());
            }
        }
        
        // 스레드 동기화를 위한 래치
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        // 성공/실패/예외 카운터
        final AtomicInteger readSuccessCount = new AtomicInteger(0);
        final AtomicInteger writeSuccessCount = new AtomicInteger(0);
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        // 여러 스레드가 동시에 다양한 작업을 수행
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    
                    // 스레드마다 랜덤하게 다른 작업 수행
                    int operationType = ThreadLocalRandom.current().nextInt(4);
                    switch (operationType) {
                        case 0: // 최저가 조회
                            productService.findLowestPriceByAllCategories();
                            readSuccessCount.incrementAndGet();
                            break;
                            
                        case 1: // 브랜드 정보 업데이트
                            Long randomBrandId = brandIds.get(ThreadLocalRandom.current().nextInt(brandIds.size()));
                            brandService.updateBrand(randomBrandId, "UpdatedBrand" + threadIndex);
                            writeSuccessCount.incrementAndGet();
                            break;
                            
                        case 2: // 상품 가격 업데이트
                            Long randomProductId = productIds.get(ThreadLocalRandom.current().nextInt(productIds.size()));
                            int newPrice = 5000 + ThreadLocalRandom.current().nextInt(20000);
                            productService.updateProduct(randomProductId, newPrice);
                            writeSuccessCount.incrementAndGet();
                            break;
                            
                        case 3: // 특정 카테고리 최저가 브랜드 조회
                            Category randomCategory = Category.values()[ThreadLocalRandom.current().nextInt(Category.values().length)];
                            productService.findLowestPriceByCategory(randomCategory);
                            readSuccessCount.incrementAndGet();
                            break;
                    }
                } catch (Exception e) {
                    // 예외 발생 시 카운트 증가 (재시도 메커니즘이 있으므로 실패가 적을 것임)
                    exceptionCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 모든 스레드 동시 시작
        startLatch.countDown();
        
        // 모든 스레드가 완료될 때까지 대기 (최대 30초)
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // 검증
        assertThat(completed).as("모든 스레드가 타임아웃 시간 내에 완료되어야 함").isTrue();
        
        // 모든 작업이 완료되었는지 확인 (성공 + 예외 처리 = 스레드 수)
        int totalOperations = readSuccessCount.get() + writeSuccessCount.get() + exceptionCount.get();
        assertThat(totalOperations).as("총 작업 수가 스레드 수와 일치해야 함").isEqualTo(threadCount);
        
        // 각 작업 결과 출력
        System.out.println("Concurrent test results - Read operations: " + readSuccessCount.get() + 
                ", Write operations: " + writeSuccessCount.get() + ", Exceptions: " + exceptionCount.get());
        
        // 쓰기 작업이 있었음에도 최저가 조회가 가능해야 함
        assertThat(productService.findLowestPriceByAllCategories()).as("동시 작업 후에도 최저가 조회가 가능해야 함").isNotEmpty();
    }
}
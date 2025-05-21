# 브랜드 코디 서비스

이 프로젝트는 다양한 브랜드와 카테고리의 상품 가격 정보를 제공하는 API 서비스입니다. 고객은 카테고리별 최저가격 조회, 단일 브랜드 최저가격 조회, 특정 카테고리의 최저/최고가격 조회가 가능하며, 운영자는 브랜드와 상품을 관리할 수 있습니다.

## 목차
1. [구현 범위](#구현-범위)
2. [기술 스택](#기술-스택)
3. [주요 기술적 고려사항](#주요-기술적-고려사항)
4. [API 명세](#api-명세)
5. [코드 빌드 및 실행 방법](#코드-빌드-및-실행-방법)
6. [테스트 실행 방법](#테스트-실행-방법)

## 구현 범위

### 1. 카테고리별 최저가격 브랜드와 상품 가격, 총액 조회
- 각 카테고리별로 최저가격을 제공하는 브랜드와 해당 가격을 조회할 수 있습니다.
- 모든 카테고리의 최저가격 합계를 계산하여 총액을 제공합니다.

### 2. 단일 브랜드 최저가격 조회
- 단일 브랜드로 모든 카테고리 상품을 구매할 경우, 총액이 가장 저렴한 브랜드를 조회합니다.
- 해당 브랜드의 각 카테고리별 가격과 총액을 제공합니다.

### 3. 특정 카테고리 최저/최고가격 브랜드 조회
- 특정 카테고리에서 최저가격을 제공하는 브랜드와 최고가격을 제공하는 브랜드를 조회합니다.
- 동일한 가격을 제공하는 브랜드가 여러 개인 경우 모두 표시합니다.

### 4. 브랜드 및 상품 관리
- 새로운 브랜드를 등록할 수 있습니다.
- 브랜드별 카테고리 상품을 추가, 변경, 삭제할 수 있습니다.

## 기술 스택
- **언어 & 프레임워크**: Java 17, Spring Boot 3.4.5
- **데이터베이스**: H2 Database
- **빌드 도구**: Gradle
- **테스트 도구**: JUnit 5, Mockito, Spring Boot Test

## 주요 기술적 고려사항

### 1. 데이터 모델링 및 스키마 설계
- **엔티티 구조**: Brand(브랜드)와 Product(상품) 엔티티를 설계하여 브랜드-상품 간 1:N 관계를 구현했습니다.
- **데이터 제약조건**: 브랜드 이름 유니크 제약조건, 브랜드-카테고리 유니크 제약조건을 통해 데이터 무결성을 보장합니다.
- **인덱스 최적화**: 
  - `idx_brand_name`: 브랜드명 검색 최적화
  - `idx_product_brand_category`: 브랜드-카테고리 조합 검색 최적화
  - `idx_product_category_price`: 카테고리별 가격 기반 검색 최적화

### 2. 동시성 제어
- **낙관적 락(Optimistic Locking)**: 엔티티에 `@Version` 필드를 도입하여 동시 수정 시 충돌을 감지하고 처리합니다.
- **레이스 컨디션 방지**: 서비스 계층에서의 중복 체크 대신 데이터베이스 제약 조건 위반 예외를 활용하여 레이스 컨디션을 방지합니다.
- **자동 재시도 메커니즘**: Spring Retry를 사용하여 낙관적 락 충돌 시 자동으로 재시도하는 로직을 구현했습니다.
- **동시성 테스트**: 두 스레드가 동시에 동일 브랜드-카테고리 상품을 생성하는 테스트를 구현하여 동시성 처리를 검증했습니다.

### 3. 성능 최적화

#### 전체 최적화 전략
- **DTO 프로젝션**:
  - 필요한 데이터만 선택적으로 조회하여 네트워크 트래픽과 메모리 사용량 최소화
  - 엔티티 대신 DTO를 직접 반환하여 변환 오버헤드 제거
  - `CategoryBrandPriceDto`, `BrandTotalProjection` 등의 DTO를 활용하여 필요한 데이터만 효율적으로 조회

- **효율적인 쿼리 설계**:
  - 서브쿼리와 조인을 활용하여 데이터베이스 레벨에서 최적화
  - 최저가/최고가를 애플리케이션에서 계산하지 않고 DB에서 바로 조회
  - 복잡한 집계 작업은 JPQL과 네이티브 쿼리를 적절히 조합하여 구현

- **인덱스 전략**:
  - 조회 패턴에 맞는 인덱스 설계로 쿼리 성능 최적화
  - 복합 인덱스를 활용하여 정렬 및 필터링 최적화
  - 카테고리-가격 인덱스로 가격 기반 검색 성능 개선

- **N+1 문제 방지**:
  - 조인 및 페치 조인을 활용하여 N+1 문제 해결
  - 단일 쿼리로 연관 데이터를 한 번에 조회
  - 특히 브랜드와 상품 정보를 함께 조회할 때 성능 개선

- **캐싱 전략**:
  - 자주 조회되고 변경이 적은 데이터에 캐싱 적용
  - 적절한 캐시 무효화 전략으로 데이터 일관성 유지
  - 상품 데이터 변경 시 관련 캐시를 자동으로 갱신하는 메커니즘 구현

#### 요구사항별 최적화 전략

1. **카테고리별 최저가 브랜드와 가격 조회**:
   - 단일 쿼리로 모든 카테고리의 최저가 상품을 조회
   - DTO 프로젝션으로 필요한 정보만 반환
   - 카테고리-가격 복합 인덱스 활용
   - 결과를 캐싱하여 빈번한 조회 성능 개선

2. **단일 브랜드로 모든 카테고리 구매 시 최저가 브랜드 조회**:
   - 브랜드별 총액을 데이터베이스에서 직접 계산
   - 모든 카테고리(8개)를 보유한 브랜드만 대상으로 필터링
   - 브랜드-카테고리 복합 인덱스 활용
   - 결과를 캐싱하여 조회 성능 최적화

3. **특정 카테고리의 최저/최고가 브랜드 조회**:
   - 파라미터화된 쿼리로 특정 카테고리만 효율적으로 조회
   - MIN/MAX 함수를 활용하여 데이터베이스 레벨에서 최적화
   - 카테고리 인덱스 활용
   - 동일 가격 브랜드를 모두 포함하는 결과 처리

4. **브랜드 및 상품 CRUD 기능**:
   - 적절한 인덱스 설계로 CRUD 성능 최적화
   - 캐시 무효화 전략으로 데이터 변경 시 일관성 유지
   - 낙관적 락을 통한 동시성 제어

### 4. 예외 처리
- **글로벌 예외 처리**: `GlobalExceptionHandler`를 구현하여 모든 예외를 중앙에서 일관되게 처리합니다.
- **상세한 예외 응답**: 예외 유형별로 적절한 HTTP 상태 코드와 오류 메시지를 제공합니다.
  - `NoSuchElementException` → 404 Not Found
  - `IllegalArgumentException` → 400 Bad Request
  - `DataIntegrityViolationException` → 409 Conflict (동시성 충돌)
  - `ObjectOptimisticLockingFailureException` → 409 Conflict (낙관적 락 충돌)
- **Validation Integration**: Spring Validation을 활용하여 입력값을 검증하고, 검증 실패 시 일관된 오류 메시지를 제공합니다.

### 5. 테스트 전략
- **계층별 테스트**: 
  - 단위 테스트: 서비스 레이어 로직 검증
  - 리포지토리 테스트: 복잡한 쿼리 메서드 검증
  - 통합 테스트: End-to-End API 검증
- **동시성 테스트**: 여러 스레드를 사용하여 동시성 시나리오 검증
- **테스트 데이터 관리**: SQL 스크립트를 사용하여 테스트마다 일관된 데이터 환경 보장

## API 명세

### 1. 카테고리별 최저가격 조회 API
- **Endpoint**: `GET /api/products/lowest-price`
- **응답 예시**:
```json
{
  "categories": [
    {
      "category": "TOP",
      "categoryDisplayName": "상의",
      "brandName": "C",
      "price": 10000,
      "formattedPrice": "10,000"
    },
    // ... 다른 카테고리
  ],
  "totalPrice": 34100,
  "formattedTotalPrice": "34,100"
}
```

### 2. 단일 브랜드 최저가격 조회 API
- **Endpoint**: `GET /api/brands/lowest-price`
- **응답 예시**:
```json
{
  "brand": "D",
  "items": [
    {
      "category": "상의",
      "price": 10100,
      "formattedPrice": "10,100"
    },
    // ... 다른 카테고리
  ],
  "totalPrice": 36100,
  "formattedTotalPrice": "36,100"
}
```

### 3. 특정 카테고리 최저/최고가격 조회 API
- **Endpoint**: `GET /api/products/category/{category}`
- **응답 예시**:
```json
{
  "category": "상의",
  "min": {
    "brand": "C",
    "price": 10000,
    "formattedPrice": "10,000"
  },
  "max": {
    "brand": "I",
    "price": 11400,
    "formattedPrice": "11,400"
  }
}
```

### 4. 브랜드 관리 API
- **브랜드 생성**: `POST /api/brands`
- **브랜드 조회**: `GET /api/brands/{id}`
- **브랜드 수정**: `PUT /api/brands/{id}`
- **브랜드 삭제**: `DELETE /api/brands/{id}`

### 5. 상품 관리 API
- **상품 생성**: `POST /api/products/brand/{brandId}`
- **상품 조회**: `GET /api/products/{id}`
- **상품 수정**: `PUT /api/products/{id}`
- **브랜드-카테고리 상품 수정**: `PUT /api/products/brand/{brandId}/category/{category}`
- **상품 삭제**: `DELETE /api/products/{id}`

## 코드 빌드 및 실행 방법

### 필요 조건
- JDK 17 이상
- Gradle 7.x 이상

### 빌드 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/Jae-KimSeo/brand-cody.git
cd brand-cody

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

기본적으로 애플리케이션은 `http://localhost:8080`에서 실행됩니다.

## 테스트 실행 방법

```bash
# 모든 테스트 실행
./gradlew test

# 단위 테스트만 실행
./gradlew test --tests "org.service.brandcody.unit.*"

# 통합 테스트만 실행
./gradlew test --tests "org.service.brandcody.integration.*"
```

---

## 추가 정보

- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)
- API 문서: `http://localhost:8080/swagger-ui` (Swagger UI)
- 초기 데이터는 `data.sql`을 통해 로드됩니다.

### 🔎 API 데모 UI
애플리케이션을 실행한 후 브라우저에서 다음 URL로 접속하여 API를 간편하게 테스트할 수 있습니다:
```
http://localhost:8080/index.html
```

API 데모 UI를 통해 다음 기능을 테스트할 수 있습니다:
1. 카테고리별 최저가 조회
2. 단일 브랜드 최저 세트 조회
3. 특정 카테고리 최고/최저가 조회
4. 브랜드 CRUD 
5. 상품 CRUD

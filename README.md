# Brand Cody Service

## 프로젝트 개요
해당 프로젝트는 8개의 카테고리(상의, 아우터, 바지, 스니커즈, 가방, 모자, 양말, 액세서리)에서 상품을 하나씩 구매하여 코디를 완성하는 서비스를 제공합니다.

## 구현 범위
1. 카테고리별 최저가격 브랜드와 상품 가격, 총액을 조회하는 API
2. 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격 브랜드와 가격, 총액을 조회하는 API
3. 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격을 조회하는 API
4. 브랜드 및 상품을 추가/업데이트/삭제하는 API

## 기술 스택
- 언어: Java 17
- 프레임워크: Spring Boot 3.4.5
- 데이터베이스: H2 (인메모리 DB)
- ORM: Spring Data JPA
- 빌드도구: Gradle

## 실행 방법
```bash
# 프로젝트 클론
git clone https://github.com/Jae-KimSeo/brand-cody.git
cd BrandCody

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

## API 문서
서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
(API 문서 추후 추가 예정)

## 프로젝트 구조
```
src/main/java/org/service/brandcody
├── BrandCodyApplication.java (메인 애플리케이션)
├── domain (도메인 엔티티)
├── repository (데이터 액세스)
├── service (비즈니스 로직)
├── controller (API 엔드포인트)
├── dto (데이터 전송 객체)
└── exception (예외 처리)
```

## 라이센스
이 프로젝트는 MIT 라이센스를 따릅니다.

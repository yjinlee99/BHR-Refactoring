# BHR Refactoring

기존 팀 프로젝트로 진행했던 인사관리 시스템을 개인적으로 다시 분석하고 리팩토링하는 프로젝트입니다.

## 프로젝트 소개

사원 정보, 근태, 연차, 부서 관리 등의 기능을 제공하는 인사관리 시스템입니다.

기존 프로젝트의 기능을 유지하면서 코드 구조, 예외 처리, 테스트, 환경 설정 등을 개선하는 것을 목표로 합니다.

## 리팩토링 목표

- 프로젝트 구조 및 패키지 정리
- Controller, Service, Repository 역할 분리
- Request / Response DTO 분리
- 공통 예외 처리 적용
- 환경별 설정 파일 분리
- 테스트 코드 보강
- 불필요한 의존성과 빌드 파일 정리
- API 문서화

## 기술 스택

- Java
- Spring Boot
- Spring Data JPA
- MariaDB
- Maven

## 진행 상황

- [x] 초기 프로젝트 및 빌드 환경 정리
- [x] 로컬 테스트 실행 환경 구성
- [x] GitHub Actions CI 구성
- [x] 프로젝트 구조 및 패키지 정리
- [ ] 공통 예외 처리 구조 개선
- [ ] 연차 도메인 리팩토링
- [ ] 근태 도메인 리팩토링
- [ ] 사원 도메인 리팩토링
- [ ] 테스트 코드 작성
- [ ] API 문서 작성

## 원본 프로젝트

본 프로젝트는 기존 팀 프로젝트를 기반으로 개인 학습 및 포트폴리오 목적으로 리팩토링한 프로젝트입니다.

원본 프로젝트 문서:

https://github.com/9-1379/T3-R0-Document

## 주요 변경 내역

<details>
<summary><strong>2026-07-14 — 테스트 및 CI 환경 정리</strong></summary>

<br>

- 운영 환경의 데이터베이스 정보와 JWT Secret을 환경변수로 분리
- 테스트 전용 `application-test.yaml` 추가
- 테스트 환경에서 인메모리 H2 데이터베이스를 사용하도록 구성
- `BhrApplicationTests`에 `test` 프로필 적용
- 테스트용 H2 의존성 추가
- GitHub Actions CI 워크플로우 구성
  - Java 17 환경 설정
  - Maven 의존성 캐시 적용
  - `./mvnw clean test` 자동 실행
- GitHub Actions를 Node.js 24 기반 버전으로 변경
  - `actions/checkout@v5`
  - `actions/setup-java@v5`

</details>

<details>
<summary><strong>2026-07-15 — 프로젝트 구조 및 패키지 정리</strong></summary>

<br>

- `annual.cotroller` 오타를 `annual.controller`로 수정
- 축약형 패키지명 `emp`를 `employee`로 변경
- 애플리케이션 공통 코드를 `global` 패키지로 분리
  - `global.config`: 공통 웹 설정
  - `global.exception`: 공통 예외 처리
  - `global.security`: Spring Security 및 JWT 인증 설정
- Security 설정과 JWT 필터를 `global.security` 아래로 이동
- ID 생성기의 문자열 경로 참조를 클래스 타입 참조 방식으로 변경
- 사용되지 않던 `BadgeInitializerRunner` 제거
- 패키지 변경에 따른 `package` 선언과 `import` 경로 정리
- 로컬 테스트 및 GitHub Actions CI 통과 확인

</details>


<details>
<summary><strong>2026-08-05 — 공통 에러 응답 구조 개선</strong></summary>

### 기존 구조의 문제

- API마다 에러 응답 구조가 문자열, 필드별 객체, Spring 기본 응답 등 서로 달랐습니다.
- 동일한 리소스 조회 실패 상황에서도 상태 코드가 `400`, `404`, `500`으로 다르게 반환되었습니다.
- 공통 에러 코드가 없어 프론트엔드에서 API별로 예외 응답을 별도로 처리해야 했습니다.
- 일부 Controller에서 직접 `try-catch`를 사용하여 예외 처리 코드가 분산되어 있었습니다.

### 공통 에러 형식 선택

Spring의 `ProblemDetail` 대신 직접 정의한 `ApiErrorResponse`를 사용했습니다.

공통 에러 응답 구조를 직접 설계하면서 각 필드의 역할과 예외 처리 흐름을 명확히 이해하고, 프로젝트에서 필요한 `timestamp`, `status`, `code`, `message`, `path`를 일관된 형식으로 제공하기 위해 선택했습니다.

### 변경한 클래스와 처리 흐름

- `ErrorCode`
  - HTTP 상태 코드, 에러 코드, 기본 메시지를 한곳에서 관리합니다.
- `ApiErrorResponse`
  - 모든 예외 응답이 동일한 필드를 사용하도록 정의했습니다.
- `BusinessException`
  - 예상 가능한 비즈니스 예외의 공통 부모 클래스로 사용했습니다.
- `BadgeNotFoundException`
  - 존재하지 않는 배지를 요청했을 때 `BADGE_NOT_FOUND` 에러 코드를 포함하여 발생하도록 변경했습니다.
- `GlobalExceptionHandler`
  - `@RestControllerAdvice`에서 비즈니스 예외와 예상하지 못한 예외를 공통 처리하도록 변경했습니다.
- `AdminBadgeController`
  - 활성화·비활성화 API의 `try-catch`와 문자열 응답을 제거하고, 성공 시 `204 No Content`를 반환하도록 변경했습니다.
- `BadgeManageService`
  - 배지를 찾을 수 없는 경우 `BadgeNotFoundException`을 발생시키도록 통일했습니다.

처리 흐름은 다음과 같습니다.

`Controller → Service → BusinessException → GlobalExceptionHandler → ApiErrorResponse`

예상하지 못한 오류는 `500 Internal Server Error`로 처리하며, 내부 예외 메시지와 스택 트레이스는 응답에 노출하지 않고 서버 로그에만 기록하도록 했습니다.

### 테스트 결과

Controller부터 실제 Service와 Repository, 전역 예외 처리까지 확인할 수 있도록 통합 테스트를 작성했습니다.

- 배지 활성화 성공: `204 No Content`
- 배지 비활성화 성공: `204 No Content`
- 존재하지 않는 배지 활성화: `404 Not Found` 및 공통 에러 응답
- 존재하지 않는 배지 비활성화: `404 Not Found` 및 공통 에러 응답
- 예상하지 못한 오류: 내부 메시지를 노출하지 않는 `500 Internal Server Error`

활성화와 비활성화 API가 동일한 에러 응답 구조를 사용하는 것을 확인했습니다.

### 아직 적용하지 않은 범위

현재 공통 에러 응답은 배지 활성화·비활성화 API에만 적용했습니다.

입력값 검증 오류는 공통 응답 형식에 포함했지만, 다음 비즈니스 및 인증 오류는 기존 처리 방식을 유지하고 있으며 추후 순차적으로 적용할 예정입니다.

</details>


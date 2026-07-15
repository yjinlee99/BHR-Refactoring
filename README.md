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


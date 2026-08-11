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


### 현재 에러 처리 상태 분석

| API | 발생 조건 | 현재 상태 코드 | 현재 응답 본문 | 문제점 |
|---|---|---|---|---|
| 배지 활성화 | 존재하지 않는 배지 이름으로 활성화 요청 | `400 Bad Request` | `배지를 찾을 수 없습니다: 없는배지` | Controller에서 `try-catch`로 예외를 직접 처리하고 일반 문자열을 반환한다. 존재하지 않는 리소스에 `400`을 사용하고 있어 상태 코드도 적절하지 않다. |
| 배지 비활성화 | 존재하지 않는 배지 이름으로 비활성화 요청 | `500 Internal Server Error` | `{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/api/admin/badge/deactivate"}` | 존재하지 않는 배지는 예상 가능한 오류인데 서버 내부 오류인 `500`으로 처리된다. 구체적인 오류 메시지와 고정된 에러 코드도 제공되지 않는다. |
| 회원가입 | 필수 입력값 없이 빈 JSON 객체 전송 | `400 Bad Request` | `birthday`, `jobId`, `password`, `hireDate`, `phoneNumber`, `gender`, `name`, `position`, `username`의 필드별 검증 메시지 객체 | 필드별 오류를 확인할 수 있지만 다른 API의 오류 응답 형식과 일치하지 않는다. 같은 시스템에서 오류 상황마다 서로 다른 JSON 구조를 사용한다. |
| 회원가입 | 이미 존재하는 아이디로 다시 가입 요청 | `400 Bad Request` | `{"message":"Username 'duplicate_test' already exists."}` | 같은 회원가입 API에서도 입력값 검증 실패 응답과 중복 아이디 응답의 구조가 다르다. Controller가 모든 `Exception`을 잡아 `400`으로 반환하므로 다른 내부 오류도 잘못 처리될 수 있다. |
| 사원 퇴직 | 존재하지 않는 사원 ID `e9999`로 퇴직 요청 | `404 Not Found` | `{"timestamp":"...","status":404,"error":"Not Found","path":"/api/employees/e9999/retire"}` | 상태 코드는 적절하지만 구체적인 실패 이유와 에러 코드가 없다. 모든 `RuntimeException`을 `404`로 처리해 다른 오류까지 사원 미존재 오류로 숨길 가능성이 있다. |
| 로그인 | 존재하는 아이디에 잘못된 비밀번호 입력 | `401 Unauthorized` | 응답 본문 없음 | 로그인 실패를 나타내는 메시지나 에러 코드가 없어 클라이언트가 실패 원인을 확인하기 어렵다. 다른 API의 오류 응답 구조와도 일치하지 않는다. |

분석 결과 API마다 일반 문자열, 필드별 객체, Spring 기본 오류 응답, 빈 응답 등 서로 다른 형식을 사용하고 있었다. 비슷한 오류 상황에서도 상태 코드가 일관되지 않았으며, 프론트엔드에서는 API별 응답 구조에 맞춰 별도의 오류 처리 로직을 작성해야 하는 문제가 있었다.


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

입력값 검증 오류는 `400 Bad Request`로 처리하고 있지만 필드별 검증 메시지 객체 형식을 사용하고 있어 현재 `ApiErrorResponse`와는 형식이 다릅니다.

중복 아이디, 사원 퇴직, 로그인 인증 실패 등 다른 비즈니스 및 인증 오류는 기존 처리 방식을 유지하고 있으며 추후 순차적으로 적용할 예정입니다.
</details>

<details>
<summary><strong>2026-08-10 — 공통 에러 처리 보완 및 오류 응답 재확인</strong></summary>

<br>

### 오류 응답 재확인

공통 에러 처리 적용 이후 기존 오류 상황을 다시 실행하여 상태 코드와 응답 형식을 확인했습니다.

같은 API에서도 요청 조건에 따라 서로 다른 예외가 발생할 수 있어 발생 조건별로 구분하여 확인했습니다.

| API | 발생 조건 | 현재 상태 코드 | 현재 응답 본문 | 문제점 |
|---|---|---|---|---|
| 배지 활성화 | 존재하지 않는 배지 이름으로 요청 | `404 Not Found` | `ApiErrorResponse` (`code: BADGE_NOT_FOUND`) | 공통 에러 응답이 정상 적용되었습니다. |
| 배지 활성화 | 빈 배지 이름 전달 (`badgeName=`) | `404 Not Found` | `ApiErrorResponse` (`code: BADGE_NOT_FOUND`) | 빈 입력값을 존재하지 않는 배지로 처리하고 있어 입력값 검증 여부를 추가로 검토할 필요가 있습니다. |
| 배지 활성화 | `badgeName` 파라미터 누락 | `500 Internal Server Error` | `ApiErrorResponse` (`code: INTERNAL_SERVER_ERROR`) | 클라이언트 요청 오류가 `Exception.class` catch-all에 의해 `500`으로 변환됩니다. |
| 배지 비활성화 | 존재하지 않는 배지 이름으로 요청 | `404 Not Found` | `ApiErrorResponse` (`code: BADGE_NOT_FOUND`) | 공통 에러 응답이 정상 적용되었습니다. |
| 배지 비활성화 | 빈 배지 이름 전달 (`badgeName=`) | `404 Not Found` | `ApiErrorResponse` (`code: BADGE_NOT_FOUND`) | 빈 입력값을 존재하지 않는 배지로 처리하고 있어 입력값 검증 여부를 추가로 검토할 필요가 있습니다. |
| 배지 비활성화 | `badgeName` 파라미터 누락 | `500 Internal Server Error` | `ApiErrorResponse` (`code: INTERNAL_SERVER_ERROR`) | 클라이언트 요청 오류가 `Exception.class` catch-all에 의해 `500`으로 변환됩니다. |
| 회원가입 | 필수 입력값 없이 빈 JSON 객체 `{}` 전송 | `400 Bad Request` | 필드별 검증 메시지 객체 | 상태 코드는 적절하지만 공통 `ApiErrorResponse` 형식과 다릅니다. |
| 회원가입 | 필수 필드 `name` 누락 | `400 Bad Request` | `{"name":"이름은 필수 사항입니다"}` | 검증 오류는 정상 처리되지만 필드별 메시지 객체 형식을 사용합니다. |
| 회원가입 | `name`에 빈 문자열 전달 | `400 Bad Request` | `{"name":"이름은 필수 사항입니다"}` | 검증 오류는 정상 처리되지만 공통 `ApiErrorResponse` 형식과 다릅니다. |
| 회원가입 | Request Body 자체 누락 | `500 Internal Server Error` | `ApiErrorResponse` (`code: INTERNAL_SERVER_ERROR`) | 잘못된 요청이지만 catch-all에 의해 `500`으로 처리됩니다. |
| 회원가입 | 잘못된 JSON 형식 전송 | `500 Internal Server Error` | `ApiErrorResponse` (`code: INTERNAL_SERVER_ERROR`) | 요청 본문 파싱 오류가 `500`으로 처리되고 있어 `400 Bad Request`로 보완할 필요가 있습니다. |
| 회원가입 | 이미 존재하는 아이디로 가입 요청 | `400 Bad Request` | `{"message":"Username 'duplicate_test' already exists."}` | 상태 코드는 적절하지만 다른 오류 응답과 구조가 다릅니다. |
| 회원가입 | 중복 아이디와 함께 필수 입력값 검증 실패 | `400 Bad Request` | 필드별 검증 메시지 객체 | DTO 검증 단계에서 요청이 종료되어 중복 아이디 검사까지 진행되지 않습니다. |
| 사원 퇴직 | 존재하지 않는 사원 ID로 요청 | `404 Not Found` | 응답 본문 없음 | Controller의 `try-catch`에서 직접 처리되어 공통 에러 응답이 적용되지 않습니다. |
| 로그인 | 존재하는 아이디에 잘못된 비밀번호 입력 | `401 Unauthorized` | 응답 본문 없음 | 인증 실패는 적절하게 `401`로 처리되지만 공통 에러 응답 형식은 적용되지 않습니다. |
| 로그인 | 존재하지 않는 아이디 입력 | `401 Unauthorized` | 응답 본문 없음 | 상태 코드는 적절하지만 에러 코드와 메시지가 없습니다. |
| 로그인 | `password` 누락 | `401 Unauthorized` | 응답 본문 없음 | 로그인 실패와 동일하게 `401`로 처리됩니다. |
| 로그인 | `username` 누락 | `401 Unauthorized` | 응답 본문 없음 | 로그인 실패와 동일하게 `401`로 처리됩니다. |
| 로그인 | `username`, `password` 모두 누락 | `401 Unauthorized` | 응답 본문 없음 | 로그인 실패와 동일하게 `401`로 처리됩니다. |

### 확인된 문제

실행 결과, 배지 미존재 오류는 리팩토링을 통해 `404 Not Found`와 `BADGE_NOT_FOUND` 공통 응답으로 정상화된 것을 확인했습니다.

반면 Spring MVC에서 클라이언트 요청 오류로 처리되어야 하는 다음 상황이 `Exception.class` catch-all에 의해 `500 Internal Server Error`로 변경되고 있음을 확인했습니다.

- 배지 활성화·비활성화의 `badgeName` 파라미터 누락
- 회원가입 Request Body 누락
- 회원가입 JSON 형식 오류

또한 오류 종류에 따라 응답 형식이 아직 다르게 사용되고 있습니다.

- 배지 업무 예외: `ApiErrorResponse`
- 입력값 검증 오류: 필드별 검증 메시지 객체
- 중복 아이디 오류: `message`만 포함한 객체
- 사원 퇴직 오류: 응답 본문 없음
- 로그인 인증 실패: 응답 본문 없음

따라서 현재 공통 에러 응답은 배지 활성화·비활성화의 업무 예외에 적용된 상태이며, 모든 MVC 및 Security 오류가 공통 형식으로 통일된 상태는 아닙니다.

### 추가 확인 사항

사원 퇴직 API를 확인하는 과정에서 인증된 사용자가 자신의 사원 ID에 대해 퇴직 요청을 수행할 수 있음을 확인했습니다.

현재 사원 퇴직 API에 별도의 역할 제한이 적용되어 있지 않아 퇴직 처리 기능의 실제 권한 정책을 추가로 확인할 필요가 있습니다.

또한 퇴직 처리는 사원 상태를 변경하지만 이미 발급된 JWT를 즉시 무효화하지 않습니다. 현재 JWT 기반 인증은 `STATELESS` 방식이므로 퇴직 전에 발급된 유효한 토큰에 대한 처리 정책도 추후 확인할 필요가 있습니다.

### 공통 에러 처리 보완

추가 실행 확인 과정에서 `Exception.class` catch-all이 Spring MVC의 일부 요청 오류를
`500 Internal Server Error`로 변환하는 문제를 확인했습니다.

다음 요청이 클라이언트 오류에 맞는 `400 Bad Request`를 유지하도록 예외 처리를 보완했습니다.

| 발생 조건 | 기존 응답 | 변경 후 응답 |
|---|---|---|
| 필수 Request Parameter 누락 | `500 INTERNAL_SERVER_ERROR` | `400 MISSING_REQUEST_PARAMETER` |
| Request Body 누락 | `500 INTERNAL_SERVER_ERROR` | `400 INVALID_REQUEST_BODY` |
| 잘못된 JSON 형식 | `500 INTERNAL_SERVER_ERROR` | `400 INVALID_REQUEST_BODY` |
| `@Valid` 입력값 검증 실패 | 필드별 메시지 객체 | `400 VALIDATION_FAILED` |

입력값 검증 실패는 필드별 오류 정보를 유지하면서 다음과 같은 형식으로 정리했습니다.

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "입력값을 확인해 주세요.",
  "errors": {
    "username": "아이디는 필수 사항입니다."
  }
}
```
### 회귀 테스트

수정한 예외 처리가 다시 기존 동작으로 돌아가지 않도록 회귀 테스트를 추가했습니다.

- 필수 `badgeName` 파라미터 누락 → `400 MISSING_REQUEST_PARAMETER`
- 회원가입 Request Body 누락 → `400 INVALID_REQUEST_BODY`
- 회원가입 JSON 형식 오류 → `400 INVALID_REQUEST_BODY`
- 회원가입 입력값 검증 실패 → `400 VALIDATION_FAILED`

이를 통해 클라이언트의 잘못된 요청이 `Exception.class` catch-all에 의해 다시 `500 Internal Server Error`로 처리되지 않는지 확인합니다.

### 테스트 데이터 분리

기존 `data.sql`에는 애플리케이션 초기화에 필요한 데이터와
배지 API 확인을 위한 테스트 데이터가 함께 포함되어 있었습니다.

```sql
'X세대',
'배지 API 정상 응답 확인을 위한 테스트 배지'
```

테스트 목적의 데이터가 기본 초기 데이터에 포함되지 않도록 역할을 분리했습니다.

- `data.sql`
  - 애플리케이션 실행에 필요한 공통 초기 데이터만 관리
- `data-local.sql`
  - Postman 등 로컬 API 확인에 필요한 샘플 데이터 관리

따라서 기존 `data.sql`의 `X세대` 테스트 배지는 제거하고
로컬 API 확인용 데이터는 `data-local.sql`로 이동했습니다.

</details>


<details>
<summary><b>2026-08-11 — JWT Refresh Token 및 Logout 기능 추가</b></summary>

### 작업 목적

기존 인증 구조는 로그인 성공 시 하나의 JWT를 발급하고 해당 토큰을 10시간 동안 사용하는 방식이었다.

이 구조에서는 Access Token이 만료되면 다시 로그인해야 하며, 서버에서 로그인 상태를 종료하거나 토큰을 재발급할 수 있는 방법이 없었다.

이를 개선하기 위해 Access Token과 Refresh Token을 분리하고, 토큰 재발급 및 로그아웃 기능을 추가하였다.

---

### 기존 구조

```text
로그인
  ↓
JWT 발급 (10시간)
  ↓
Authorization 헤더로 전달
  ↓
JWTFilter에서 검증
  ↓
API 접근
```

#### 기존 구조의 문제점

- Access Token과 Refresh Token의 구분이 없음
- JWT 유효 시간이 10시간으로 길게 설정됨
- Access Token 만료 시 재로그인 필요
- 서버에서 로그아웃 상태를 관리할 방법이 없음
- Authorization 헤더의 JWT 원문이 서버 로그에 출력됨

---

### 개선 내용

Access Token과 Refresh Token을 분리하였다.

| 구분 | 유효 시간 | 용도 |
| --- | --- | --- |
| Access Token | 30분 | 일반 API 인증 |
| Refresh Token | 7일 | Access Token 재발급 |

Refresh Token은 DB에 저장하여 서버에서 상태를 관리하도록 구성하였다.

---

### 로그인

로그인 성공 시 Access Token과 Refresh Token을 함께 발급한다.

```text
로그인 요청
  ↓
LoginFilter
  ↓
사용자 인증
  ↓
Access Token 생성
Refresh Token 생성
  ↓
Refresh Token DB 저장
  ↓
응답 헤더로 전달
```

응답 헤더:

```http
Authorization: Bearer <Access Token>
Refresh-Token: Bearer <Refresh Token>
```

기존 `Authorization` 헤더를 그대로 사용하여 기존 Access Token 인증 방식은 유지하였다.

---

### Access Token / Refresh Token 구분

JWT 내부에 `category` 값을 추가하여 토큰의 종류를 구분하였다.

```text
Access Token
category = access

Refresh Token
category = refresh
```

일반 API 요청에서는 `category`가 `access`인 토큰만 인증에 사용할 수 있도록 `JWTFilter`를 수정하였다.

따라서 Refresh Token을 `Authorization` 헤더에 넣어 일반 API를 요청할 경우 인증되지 않는다.

---

### Access Token 재발급

Access Token이 만료된 경우 Refresh Token을 이용하여 새로운 토큰을 발급받을 수 있도록 API를 추가하였다.

```http
POST /api/auth/refresh
```

요청 헤더:

```http
Refresh-Token: Bearer <Refresh Token>
```

재발급 흐름:

```text
Refresh Token 전달
  ↓
JWT 검증
  ↓
Refresh Token 여부 확인
  ↓
DB에 저장된 토큰인지 확인
  ↓
새 Access Token 생성
  ↓
새 Refresh Token 생성
  ↓
기존 Refresh Token 교체
  ↓
새 Access / Refresh Token 반환
```

응답 헤더:

```http
Authorization: Bearer <New Access Token>
Refresh-Token: Bearer <New Refresh Token>
```

---

### Refresh Token Rotation

Refresh Token 재사용을 방지하기 위해 Rotation 방식을 적용하였다.

```text
Refresh Token A
      ↓
POST /api/auth/refresh
      ↓
Access Token B 생성
Refresh Token B 생성
      ↓
DB
Refresh A → Refresh B
```

한 번 사용한 Refresh Token은 새로운 Refresh Token으로 교체된다.

따라서 이전 Refresh Token을 다시 사용하면 DB에서 조회되지 않아 재발급에 실패한다.

---

### Logout

로그아웃 API를 추가하였다.

```http
POST /api/auth/logout
```

요청 헤더:

```http
Refresh-Token: Bearer <Refresh Token>
```

로그아웃 흐름:

```text
로그아웃 요청
  ↓
Refresh Token 전달
  ↓
DB에 저장된 Refresh Token 삭제
  ↓
204 No Content
```

로그아웃 후 삭제된 Refresh Token으로 다시 `/api/auth/refresh`를 요청하면 Access Token을 재발급받을 수 없다.

---

### 보안 개선

기존 `JWTFilter`에서는 Authorization 헤더 전체를 콘솔에 출력하고 있었다.

```java
System.out.println(authorization);
```

Authorization 헤더에는 실제 JWT가 포함되어 있기 때문에 서버 로그를 통해 토큰이 노출될 가능성이 있다.

따라서 JWT 원문을 출력하는 로그를 제거하였다.

또한 Refresh Token을 일반 API 인증 용도로 사용할 수 없도록 Access Token과 Refresh Token의 역할을 분리하였다.

---

### 테스트 결과

Postman을 이용하여 다음 시나리오를 확인하였다.

| 테스트 | 결과 |
| --- | --- |
| 로그인 | Access Token / Refresh Token 발급 성공 |
| Access Token으로 일반 API 호출 | 정상 처리 |
| Refresh Token으로 일반 API 호출 | `401 Unauthorized` |
| Refresh Token으로 토큰 재발급 | `200 OK` |
| 새로운 Access / Refresh Token 발급 | 정상 처리 |
| 사용한 Refresh Token 재사용 | `401 Unauthorized` |
| 최신 Refresh Token으로 재발급 | 정상 처리 |
| 로그아웃 | `204 No Content` |
| 로그아웃한 Refresh Token으로 재발급 | `401 Unauthorized` |


</details>


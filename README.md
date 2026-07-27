# READTHEM.md

개발자들의 도서 리뷰 공유 플랫폼

- 프론트엔드 배포: [READTHEM.md](https://readthem-md.pages.dev)
- 백엔드 API: [Swagger API 문서](https://powell-remix-off-grade.trycloudflare.com/swagger-ui/index.html)

## 역할 분담

| 팀원   | 역할 | 담당업무                   |
| ------ | ---- | -------------------------- |
| 이호영 | 팀장 | 배포                       |
| 김민준 | 팀원 | 도서 정보 수집             |
| 이지헌 | 팀원 | 소셜 로그인, 추천 알고리즘 |

## 주요 기능

- 도서 검색 및 상세 조회
- 인기 도서, 후기 좋은 도서 랭킹
- 리뷰 작성, 수정, 삭제
- 찜 목록 관리
- GitHub 로그인 기반 회원 기능
- 관리자용 도서, 회원, 리뷰 관리
- 개인 책장 위젯 생성 및 공유
  ![Readthem.md](https://powell-remix-off-grade.trycloudflare.com/api/v1/widgets/puppywimy)

## 기술 스택

### Backend

- **Core**: Java 25, Spring Boot 4.0.6
- **Database & ORM**: MySQL(배포 환경), H2 Database (In-Memory)(개발 환경), Redis, Spring Data JPA
- **API Documentation**: Springdoc OpenAPI (Swagger)
- **Other**: Lombok, Spring Validation, Spring AOP (상태코드 매핑용 Aspect)

### Frontend

- **Framework**: Next.js 16.2.9, React 19.2.4

## 프로젝트 구조

<details>
    <summary>front</summary>
  <pre>
  src
  ├── app
  │   ├── admin
  │   ├── books
  │   │   ├── detail
  │   │   └── search
  │   ├── members
  │   │   ├── detail
  │   │   ├── join
  │   │   └── login
  │   └── mypage
  └── lib
      ├── auth
      ├── backend
      │   └── apiV1
      └── theme
  </pre>
</details>
<details>
    <summary>back</summary>
    <pre>
src/main/java/com/back
├── domain
│   ├── book
│   ├── home
│   ├── member
│   ├── review
│   ├── tag
│   ├── widget
│   └── wish
├── global
│   ├── app
│   ├── aspect
│   ├── exception
│   ├── globalExceptionHandler
│   ├── initData
│   ├── jpa
│   ├── redis
│   ├── rq
│   ├── rsData
│   ├── security
│   └── springDoc
└── standard
    ├── recommend
    │   ├── byContent
    │   ├── byRating
    │   └── util
    └── util
    
</pre>
</details>

## 실행 환경

- Node.js 20 이상 권장
- pnpm 또는 npm
- Java 25
- Spring Boot, MySQL, Redis

## 환경 변수

각 패키지에는 예시 파일이 함께 있습니다.

- `front/.env.example`
- `back/.env.example`
- `widget/.env.example`

필요한 값을 복사해서 각 `.env` 파일로 만들어 사용하세요.

### `front/.env`

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_FRONTEND_BASE_URL=http://localhost:3000
```

### `back/.env`

```env
SPRING__PROFILES__ACTIVE=dev

SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__GITHUB__CLIENT_ID=YOUR_GITHUB_CLIENT_ID
SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__GITHUB__CLIENT_SECRET=YOUR_GITHUB_CLIENT_SECRET

CUSTOM__JWT__SECRET_KEY=YOUR_JWT_SECRET
CUSTOM__JWT__EXPIRATION_MINUTES=60
CUSTOM__BOOK__FETCH__API_KEYS=
CUSTOM__SECURITY__ALLOWED_ORIGINS=http://localhost:3000
CUSTOM__SECURITY__COOKIE_DOMAIN=localhost
```

프로덕션 환경에서는 추가로 데이터베이스 접속 정보가 필요합니다.

```env
PROD__SPRING__DATASOURCE__URL=
PROD__SPRING__DATASOURCE__USERNAME=
PROD__SPRING__DATASOURCE__PASSWORD=
```

## 실행 방법

개발 서버는 보통 `back`, `front`, `widget` 순서로 올리면 편합니다.

### 1. 백엔드 실행

```bash
cd back
./gradlew bootRun
```

개발 환경에서는 H2를 사용할 수 있고, `application-dev.yaml` 기준으로 로컬 DB 파일이 생성됩니다.
Redis, GitHub OAuth, JWT 관련 값은 `back/.env`에 함께 설정해야 합니다.

### 2. 프론트엔드 실행

```bash
cd front
pnpm install
pnpm dev
```

브라우저에서 `http://localhost:3000`으로 접속합니다.

### 3. 위젯 패키지

`widget` 디렉터리는 위젯 렌더링 서버입니다.

```bash
cd widget
npm install
npm start
```

기본 실행 주소는 `http://localhost:3001`입니다.

## API 개요

백엔드는 기본적으로 `/api/v1/**` 경로의 REST API를 제공합니다.

- `/api/v1/books`
- `/api/v1/reviews`
- `/api/v1/members`
- `/api/v1/wishes`
- `/api/v1/tags`
- `/api/v1/widgets`

OpenAPI 문서는 백엔드 실행 후 `http://localhost:8080/swagger-ui/index.html`에서 확인할 수 있습니다.

## 개발 스크립트

### `front`

- `pnpm dev`: 개발 서버 실행
- `pnpm build`: 프로덕션 빌드
- `pnpm start`: 빌드된 앱 실행
- `pnpm lint`: ESLint 실행
- `pnpm format`: Prettier로 포맷
- `pnpm tsc`: 타입 검사
- `pnpm check`: 포맷, 타입 검사, 린트 순차 실행

### `back`

- `./gradlew bootRun`: 서버 실행
- `./gradlew test`: 테스트 실행
- `./gradlew bootJar`: 실행 JAR 생성

## 브랜치 전략

- `main`: GitHub Actions를 통해 배포되는 운영 브랜치
  - 직접 push 불가
- `dev`: 개발 내용을 모으는 통합 브랜치
  - 직접 push 불가
  - 한 명의 승인 후 `main`으로 병합 가능
- `feat`: 세부 기능별 작업 브랜치
  - 한 명의 승인 후 `dev`로 병합 가능

## 참고

- 프론트는 `NEXT_PUBLIC_API_BASE_URL`을 통해 백엔드와 통신합니다.
- 로그인과 위젯 링크는 GitHub OAuth와 연동되어 있습니다.
- 위젯 렌더러 기본 주소는 `http://localhost:3001`입니다.

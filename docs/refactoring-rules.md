# Refactoring Review Rules

이 문서는 `refactor` PR에서 Gemini 코드 리뷰가 추가로 확인해야 하는 리팩토링 규칙입니다.

## 검토 원칙

- 리팩토링 PR은 기존 기능의 외부 동작을 변경하지 않아야 합니다.
- Swagger/OpenAPI에 노출되는 도메인 이름, API 설명, 성공 메시지, 오류 메시지는 이 문서의 용어를 따라야 합니다.
- 예외 항목으로 명시된 API는 아래 API 명 규칙과 정확히 일치하지 않아도 됩니다.
- 규칙 위반이 의심되면 변경된 코드 위치와 기대되는 이름을 함께 지적합니다.

## 도메인 용어

Swagger/OpenAPI에서 도메인이나 기능을 설명할 때 아래 한글 이름을 사용합니다.
아래 용어와 다른 표현을 사용하면 리팩토링 규칙 위반으로 봅니다.

| 코드/도메인 이름 | Swagger 설명 이름 |
| --- | --- |
| Member | 회원 |
| Member Me | 내 정보 |
| Member Mine | 내 |
| Member Username | Username |
| Widget | 위젯 |
| Review | 리뷰 |
| Book | 도서 |
| Book Recommend | 추천 |
| Book Rank | 순위 |
| Book Search | 검색 |
| Wishes | 찜 |

## API 명

Swagger/OpenAPI에서 API 동작을 설명할 때 HTTP 메서드별 이름은 아래 기준을 따릅니다.

| HTTP 메서드 | API 명 |
| --- | --- |
| POST | 생성 |
| GET, 목록/페이지/다건 조회 | 다건 조회 |
| GET, 단건 조회 | 단건 조회 |
| PUT | 수정 |
| DELETE | 삭제 |

예외 API는 위 API 명과 정확히 일치하지 않아도 됩니다.

- 회원가입
- 로그인
- 로그아웃
- 회원 탈퇴

## 메시지

성공 메시지는 아래 형식을 따릅니다.

```text
{API 명}을 성공했습니다.
```

오류 메시지는 상태 코드별로 아래 형식을 따릅니다.

| 상태 코드 | 오류 메시지 형식 |
| --- | --- |
| 409 | 이미 존재하는 {대상}입니다. |
| 404 | 존재하지 않는 {대상}입니다. |
| 403 | {API 명} 권한이 없습니다. |

## DTO 이름

DTO 클래스 이름은 아래 규칙을 따릅니다.

- Request Body DTO는 `{도메인}{행위}RequestDto` 형식을 사용합니다.
- Response DTO는 `{도메인}{행위}ResponseDto` 형식을 사용합니다.
- 공통 응답 래퍼인 `RsData`를 DTO 이름에 포함하지 않습니다.

Review 도메인의 DTO 이름 예시는 아래와 같습니다.

- 리뷰 작성 요청: `ReviewCreateRequestDto`
- 리뷰 수정 요청: `ReviewUpdateRequestDto`
- 리뷰 기본 응답: `ReviewDto`
- GitHub 정보와 평점을 포함한 리뷰 응답: `ReviewWithGithubAndRatingDto`

## Controller 규칙

- Controller의 액션 메서드 이름은 대응되는 Service 메서드 이름과 동일하게 맞춥니다.
- Controller에서 비즈니스 로직을 직접 처리하지 않고 Service에 위임합니다.

## Service 규칙

- 같은 도메인의 Service 메서드 이름은 일관된 동사와 도메인 이름을 사용합니다.
- Service 간 직접 참조는 피하고, 필요한 데이터 접근은 Repository를 직접 주입해서 처리합니다.
- 아래 Review 도메인 예시를 다른 도메인에도 같은 방식으로 적용합니다.

| 동작 | Review 도메인 Service 메서드 예시 |
| --- | --- |
| 단건 조회 | `getReview` |
| 다건 조회 | `getReviews`, `getReviewsById` |
| 생성 | `createReview` |
| 삭제 | `deleteReview` |
| 수정 | `updateReview` |

"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useEffect, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { ratingColor } from "@/lib/ratingColor";

import Avatar from "@/app/_components/Avatar";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";
import RoughButton from "@/app/_components/RoughButton";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput } from "@/app/_components/RoughInput";
import WidgetGuideModal from "@/app/_components/WidgetGuideModal";

type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];
type BookDto = components["schemas"]["BookDto"];

export default function Page() {
  const router = useRouter();
  const { loginMember, isLogin, isLoginMemberPending, refresh } = useAuth();

  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);
  const [wishes, setWishes] = useState<BookDto[] | null>(null);
  const [bookTitles, setBookTitles] = useState<Record<number, string>>({});
  const [tab, setTab] = useState<"reviews" | "wishes">("reviews");
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isWidgetGuideOpen, setIsWidgetGuideOpen] = useState(false);
  const [copiedWidgetLink, setCopiedWidgetLink] = useState(false);

  const loadReviews = () => {
    apiFetch(`/api/v1/reviews/member/mine`)
      .then((data: ReviewsByMemberDto) => {
        setLoadError(null);
        setReviewData(data);

        const bookIds = [...new Set(data.results.map((r) => r.bookId))];
        Promise.all(
          bookIds.map((bookId) =>
            apiFetch(`/api/v1/books/${bookId}`).then(
              (book) => [bookId, book.title] as const,
            ),
          ),
        )
          .then((entries) => setBookTitles(Object.fromEntries(entries)))
          .catch(() => {});
      })
      .catch((error) => {
        setLoadError(`${error.resultCode} : ${error.message}`);
      });
  };

  const loadWishes = () => {
    apiFetch(`/api/v1/wishes/mine`)
      .then((data) => {
        setLoadError(null);
        setWishes(data);
      })
      .catch((error) => {
        setLoadError(`${error.resultCode} : ${error.message}`);
      });
  };

  useEffect(() => {
    if (isLoginMemberPending) return;

    if (!isLogin) {
      router.replace(`/`);
      return;
    }

    loadReviews();
    loadWishes();
  }, [isLoginMemberPending, isLogin, router]);

  const handleDeleteReview = (reviewId: number) => {
    if (!confirm("리뷰를 삭제하시겠습니까?")) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, { method: "DELETE" })
      .then((data) => {
        alert(data.message);
        loadReviews();
      })
      .catch((error) => {
        alert(`${error.resultCode} : ${error.message}`);
      });
  };

  const handleRemoveWish = (bookId: number) => {
    apiFetch(`/api/v1/wishes/book/${bookId}`, { method: "DELETE" })
      .then((data) => {
        alert(data.message);
        loadWishes();
      })
      .catch((error) => {
        alert(`${error.resultCode} : ${error.message}`);
      });
  };

  const handleWithdraw = () => {
    if (!confirm("정말 탈퇴하시겠습니까? 되돌릴 수 없습니다.")) return;

    apiFetch(`/api/v1/members`, { method: "DELETE" })
      .then((data) => {
        alert(data.message);
        return refresh();
      })
      .then(() => {
        router.replace(`/`);
      })
      .catch((error) => {
        alert(`${error.resultCode} : ${error.message}`);
      });
  };

  const handleLogout = () => {
    apiFetch(`/api/v1/members/logout`, { method: "DELETE" }).then(() => {
      refresh().then(() => router.replace(`/`));
    });
  };

  const handleCopyWidgetLink = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedWidgetLink(true);
      window.setTimeout(() => setCopiedWidgetLink(false), 2000);
    } catch {
      alert("위젯 링크 복사에 실패했습니다. 주소를 직접 선택해 복사해 주세요.");
    }
  };

  const handleOpenWidgetGuide = () => {
    console.log("[mypage] README guide button clicked");
    console.log("[mypage] widget guide open before:", isWidgetGuideOpen);
    console.log("[mypage] widget link:", loginMember?.widgetLink);
    setIsWidgetGuideOpen((current) => !current);
  };

  useEffect(() => {
    console.log("[mypage] widget guide open state changed:", isWidgetGuideOpen);
  }, [isWidgetGuideOpen]);

  if (loadError != null) {
    return (
      <div>오류가 발생했습니다: {loadError} (백엔드 확인이 필요합니다)</div>
    );
  }

  if (
    isLoginMemberPending ||
    !isLogin ||
    reviewData == null ||
    wishes == null
  ) {
    return <div>로딩중...</div>;
  }

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;

  return (
    <div className="flex gap-8">
      <div className="flex flex-col items-center gap-1 w-48 shrink-0">
        <Avatar
          label={loginMember?.githubId ?? loginMember?.username ?? null}
          size="lg"
        />
        <div className="mt-2 text-sm">아이디 : {loginMember?.username}</div>
        <div className="text-sm">닉네임 : {loginMember?.githubId}</div>
        {loginMember?.githubLink && (
          <a
            className="theme-link text-sm underline"
            href={loginMember.githubLink}
            target="_blank"
            rel="noreferrer"
          >
            github 주소
          </a>
        )}

        {averageNumber != null && (
          <div
            className={`text-lg font-bold mt-2 ${ratingColor(averageNumber)}`}
          >
            <RatingValue rating={averageNumber} />
          </div>
        )}
        <div className="text-xs theme-muted">내가 준 평균 별점</div>
        <RatingHistogram rating={reviewData.rating} className="mt-3 w-full" />

        <RoughButton
          className="mt-4"
          roughSize="sm"
          type="button"
          onClick={handleLogout}
        >
          로그아웃
        </RoughButton>
        <RoughButton
          className="mt-2"
          roughSize="sm"
          tone="cancel"
          type="button"
          onClick={handleWithdraw}
        >
          회원 탈퇴
        </RoughButton>
      </div>

      <div className="flex-1 flex flex-col gap-4">
        <div>
          <div className="flex items-center justify-between gap-2">
            <h2 className="font-bold">위젯 미리보기</h2>
            <RoughButton
              roughSize="sm"
              type="button"
              onClick={handleOpenWidgetGuide}
            >
              README 가이드 보기
            </RoughButton>
          </div>
          <div className="rough-panel-border relative mt-1 flex min-h-24 items-center justify-center bg-transparent p-2">
            <RoughFrame className="rough-overlay" variant="card" />
            {loginMember?.githubId ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={`${API_BASE_URL}/api/v1/widgets/${loginMember.githubId}`}
                alt="내 서재 위젯"
              />
            ) : (
              <span className="text-sm theme-muted">위젯 정보가 없습니다</span>
            )}
          </div>
          {loginMember?.widgetLink && (
            <div className="mt-1">
              <RoughInput
                inputClassName="text-xs"
                roughSize="sm"
                readOnly
                value={loginMember.widgetLink}
                onFocus={(e) => e.currentTarget.select()}
              />
            </div>
          )}
        </div>

        <div className="flex gap-2">
          <div className="theme-tab">
            <button
              type="button"
              className={`px-3 py-2 text-sm ${
                tab === "reviews" ? "theme-tab-active" : "theme-tab-inactive"
              }`}
              onClick={() => setTab("reviews")}
            >
              작성한 리뷰 {reviewData.results.length}
            </button>
            {tab === "reviews" && (
              <RoughDivider
                className="theme-tab-divider"
                color="var(--foreground)"
                emphasis
                strokeWidth={1.25}
              />
            )}
          </div>
          <div className="theme-tab">
            <button
              type="button"
              className={`px-3 py-2 text-sm ${
                tab === "wishes" ? "theme-tab-active" : "theme-tab-inactive"
              }`}
              onClick={() => setTab("wishes")}
            >
              보고 싶어요 {wishes.length}
            </button>
            {tab === "wishes" && (
              <RoughDivider
                className="theme-tab-divider"
                color="var(--foreground)"
                emphasis
                strokeWidth={1.25}
              />
            )}
          </div>
        </div>

        {tab === "reviews" && (
          <>
            {reviewData.results.length === 0 && (
              <div className="text-sm theme-muted">작성한 리뷰가 없습니다.</div>
            )}

            <ul className="flex w-full flex-col">
              {reviewData.results.map((review) => (
                <li
                  key={review.id}
                  className="relative flex items-start gap-3 py-3"
                >
                  <RoughDivider fullWidth />
                  <div className="flex-1 min-w-0">
                    <Link
                      className="font-semibold hover:underline"
                      href={`/books/detail?id=${review.bookId}`}
                    >
                      {bookTitles[review.bookId] ?? `책 #${review.bookId}`}
                    </Link>

                    <div className="flex flex-wrap gap-1 text-xs theme-tag">
                      {review.tags.map((tag) => (
                        <span key={tag}>#{tag}</span>
                      ))}
                    </div>

                    <div className="text-sm mt-1">{review.content}</div>
                    <div className="mt-1 text-xs theme-subtle">
                      {review.createdDate}
                    </div>

                    <RoughButton
                      className="mt-1 px-2"
                      roughSize="sm"
                      tone="cancel"
                      type="button"
                      onClick={() => handleDeleteReview(review.id)}
                    >
                      삭제
                    </RoughButton>
                  </div>

                  <span
                    className={`font-bold shrink-0 ${ratingColor(review.rating)}`}
                  >
                    <RatingValue rating={review.rating} />
                  </span>
                </li>
              ))}
            </ul>
          </>
        )}

        {tab === "wishes" && (
          <>
            {wishes.length === 0 && (
              <div className="text-sm theme-muted">
                보고 싶어요 한 도서가 없습니다.
              </div>
            )}

            <ul className="flex w-full flex-col">
              {wishes.map((book) => (
                <li
                  key={book.id}
                  className="relative flex items-center justify-between gap-3 py-3"
                >
                  <RoughDivider fullWidth />
                  <Link
                    href={`/books/detail?id=${book.id}`}
                    className="font-semibold"
                  >
                    {book.title}
                  </Link>
                  <div className="flex items-center gap-3">
                    <span
                      className={`font-bold ${ratingColor(book.averageRating)}`}
                    >
                      <RatingValue rating={book.averageRating} />
                    </span>
                    <RoughButton
                      className="px-2"
                      roughSize="sm"
                      tone="wishActive"
                      type="button"
                      onClick={() => handleRemoveWish(book.id)}
                    >
                      보고 싶어요 취소
                    </RoughButton>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </div>

      {isWidgetGuideOpen && (
        <WidgetGuideModal
          copiedWidgetLink={copiedWidgetLink}
          onCancel={() => setIsWidgetGuideOpen(false)}
          onCopyWidgetLink={handleCopyWidgetLink}
          widgetLink={`${API_BASE_URL}/api/v1/widgets/${loginMember?.githubId}`}
        />
      )}
    </div>
  );
}

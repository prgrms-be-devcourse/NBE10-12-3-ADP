"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { ratingColor } from "@/lib/ratingColor";
import { ratingFillColor } from "@/lib/ratingColor";
import { useToast } from "@/lib/toast/ToastProvider";

import Avatar from "@/app/_components/Avatar";
import LoginRequiredModal from "@/app/_components/LoginRequiredModal";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";
import { RoughStarIcon } from "@/app/_components/RatingValue";
import ReviewFormModal from "@/app/_components/ReviewFormModal";
import RoughButton from "@/app/_components/RoughButton";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput, RoughTextarea } from "@/app/_components/RoughInput";
import RoughRatingInput from "@/app/_components/RoughRatingInput";

type BookDetailDto = components["schemas"]["BookDetailDto"];
type BookDetailWithWishId = BookDetailDto & {
  isWished?: boolean;
  wished?: boolean;
  wishId?: number | null;
};
type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"];

function WishIcon() {
  return (
    <svg
      aria-hidden="true"
      className="inline-block h-4 w-4 shrink-0 align-[-0.125em]"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </svg>
  );
}

function ReviewIcon() {
  return (
    <svg
      aria-hidden="true"
      className="inline-block h-4 w-4 shrink-0 align-[-0.125em]"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />
    </svg>
  );
}

function BookDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const { loginMember, isLogin } = useAuth();
  const { showToast, showErrorToast } = useToast();

  const [book, setBook] = useState<BookDetailWithWishId | null>(null);
  const [reviews, setReviews] = useState<ReviewDto[] | null>(null);
  const [recommendBooks, setRecommendBooks] = useState<BookDto[] | null>(null);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [showWriteForm, setShowWriteForm] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);

  const loadBook = () => {
    if (id == null) return;

    apiFetch(`/api/v1/books/${id}`)
      .then((data) => {
        setBook(data);
      })
      .catch(goToErrorPage);
  };

  const loadReviews = () => {
    if (id == null) return;

    apiFetch(`/api/v1/reviews/book/${id}`)
      .then((data) => {
        setReviews(data);
      })
      .catch(goToErrorPage);
  };

  useEffect(() => {
    if (id == null) {
      goToErrorPage({ message: "책 ID가 없습니다." });
      return;
    }

    apiFetch(`/api/v1/books/${id}`)
      .then((data) => {
        setBook(data);
      })
      .catch(goToErrorPage);

    apiFetch(`/api/v1/reviews/book/${id}`)
      .then((data) => {
        setReviews(data);
      })
      .catch(goToErrorPage);
  }, [id]);

  useEffect(() => {
    if (!isLogin) return;

    apiFetch("/api/v1/books/recommend")
      .then((data) => {
        setRecommendBooks(data);
      })
      .catch(goToErrorPage);
  }, [isLogin]);

  const extractReviewFields = (form: HTMLFormElement) => {
    const ratingInput = form.elements.namedItem("rating") as HTMLInputElement;
    const contentInput = form.elements.namedItem(
      "content",
    ) as HTMLTextAreaElement;
    const tagsInput = form.elements.namedItem("tags") as HTMLInputElement;

    contentInput.value = contentInput.value.trim();
    /*
    if (contentInput.value.length < 2) {
      alert("리뷰 내용을 2자 이상 입력해주세요.");
      contentInput.focus();
      return null;
    }
    */

    if (contentInput.value.length > 500) {
      alert("리뷰 내용은 500자 이하로 입력해주세요.");
      contentInput.focus();
      return null;
    }

    const tags = tagsInput.value
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    const ratingValue = ratingInput.value.trim();
    const rating = ratingValue === "" ? undefined : Number(ratingValue);

    return {
      content: contentInput.value,
      tags,
      ...(rating != null ? { rating } : {}),
    };
  };

  const handleWriteSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (id == null) return;

    const form = e.currentTarget;
    const body = extractReviewFields(form);
    if (body == null) return;

    apiFetch(`/api/v1/reviews/book/${id}`, {
      method: "POST",
      body: JSON.stringify(body),
    })
      .then((data) => {
        alert(data.message);
        form.reset();
        setShowWriteForm(false);
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  const handleEditSubmit = (
    e: React.SyntheticEvent<HTMLFormElement>,
    reviewId: number,
  ) => {
    e.preventDefault();

    const form = e.currentTarget;
    const body = extractReviewFields(form);
    if (body == null) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    })
      .then((data) => {
        alert(data.message);
        setEditingReviewId(null);
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  const handleToggleWish = () => {
    if (book == null || id == null) return;

    const wishId = book.wishId;

    if (wishId != null) {
      apiFetch(`/api/v1/wishes/${wishId}`, { method: "DELETE" })
        .then((data) => {
          showToast(data?.message ?? "보고 싶어요를 취소했습니다.");
          setBook((currentBook) =>
            currentBook == null
              ? currentBook
              : { ...currentBook, wishId: null },
          );
          loadBook();
        })
        .catch(showErrorToast);
      return;
    }

    apiFetch(`/api/v1/wishes/book/${id}`, { method: "POST" })
      .then((data) => {
        showToast(data?.message ?? "보고 싶어요에 추가했습니다.");
        const createdWishId = data?.data?.id;

        if (typeof createdWishId === "number") {
          setBook((currentBook) =>
            currentBook == null
              ? currentBook
              : { ...currentBook, wishId: createdWishId },
          );
        }

        loadBook();
      })
      .catch(showErrorToast);
  };

  const openLoginModal = () => {
    setShowLoginModal(true);
  };

  const handleOpenReviewAction = () => {
    if (isLogin) {
      setShowWriteForm(true);
      return;
    }
    openLoginModal();
  };

  const closeLoginModal = () => {
    setShowLoginModal(false);
  };

  const handleDelete = (reviewId: number) => {
    if (!confirm("리뷰를 삭제하시겠습니까?")) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, {
      method: "DELETE",
    })
      .then((data) => {
        alert(data.message);
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  if (id == null) {
    return <div>로딩중...</div>;
  }

  if (book == null || reviews == null) return <div>로딩중...</div>;

  const isBookWished = book.wishId != null || book.isWished || book.wished;
  const average = book.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;

  return (
    <div className="flex flex-col gap-6 p-4 max-w-3xl mx-auto w-full">
      {showLoginModal && <LoginRequiredModal onCancel={closeLoginModal} />}
      {showWriteForm && (
        <ReviewFormModal
          onCancel={() => setShowWriteForm(false)}
          onSubmit={handleWriteSubmit}
          submitLabel="리뷰 작성"
          title="리뷰 작성"
        />
      )}

      <div className="flex gap-6">
        <div className="flex w-40 shrink-0 flex-col gap-2">
          <div
            className={`rough-cover flex h-56 items-center justify-center overflow-hidden ${
              book.imgUrl ? "" : "book-cover-placeholder"
            }`}
          >
            <RoughFrame className="rough-overlay" variant="card" />
            {book.imgUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={book.imgUrl}
                alt={book.title}
                className="w-full h-full object-cover"
              />
            ) : (
              <span className="text-gray-400 text-sm">표지 없음</span>
            )}
          </div>

          {isLogin ? (
            <RoughButton
              fullWidth
              roughSize="sm"
              tone={isBookWished ? "wishActive" : "wish"}
              type="button"
              onClick={handleToggleWish}
            >
              <WishIcon />
              {isBookWished ? "보고 싶어요 취소" : "보고 싶어요"}
            </RoughButton>
          ) : (
            <RoughButton
              fullWidth
              roughSize="sm"
              tone="wish"
              type="button"
              onClick={openLoginModal}
            >
              <WishIcon />
              보고 싶어요
            </RoughButton>
          )}
        </div>

        <div className="grid flex-1 gap-4 md:grid-cols-[minmax(0,1fr)_10rem]">
          <div className="flex min-w-0 flex-col gap-2">
            <h1 className="text-2xl font-bold">{book.title}</h1>
            <div className="text-sm theme-muted">
              {book.authors.join(", ") || "-"} · {book.publisher} ·{" "}
              {book.publishedDate}
            </div>

            <p className="mt-1 text-sm theme-description">{book.description}</p>

            <div className="flex flex-wrap gap-2 text-sm theme-tag">
              {book.tags.map((tag) => (
                <span key={tag}>#{tag}</span>
              ))}
            </div>
          </div>

          <div className="flex min-w-0 flex-col gap-2">
            <div className="text-right">
              <div
                className={`text-4xl font-bold leading-none ${
                  averageNumber != null ? ratingColor(averageNumber) : ""
                }`}
              >
                {averageNumber != null ? (
                  <RatingValue rating={averageNumber} starClassName="h-9 w-9" />
                ) : (
                  "-"
                )}
              </div>
              <div className="mt-1 text-sm theme-muted">
                리뷰 {book.reviewCount}개
              </div>
            </div>
            {book.rating && (
              <RatingHistogram
                rating={book.rating}
                className="mt-1 w-full max-w-40"
              />
            )}
          </div>
        </div>
      </div>

      {isLogin && (
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-lg font-bold">추천 도서</h2>
            <span className="text-sm theme-muted">
              {recommendBooks?.length ?? 0}권
            </span>
          </div>

          {recommendBooks == null ? (
            <div className="mt-2 text-sm theme-muted">
              추천 도서를 불러오는 중...
            </div>
          ) : recommendBooks.length === 0 ? (
            <div className="mt-2 text-sm theme-muted">
              리뷰를 추가하여 추천을 받아보세요.
            </div>
          ) : (
            <ul className="book-scroll-list mt-3 flex gap-3 overflow-x-auto py-2 pb-4">
              {recommendBooks.map((recommendBook) => (
                <li key={recommendBook.id} className="min-w-0 shrink-0">
                  <Link
                    className="book-link group flex h-full w-24 flex-col gap-1.5"
                    href={`/books/detail?id=${recommendBook.id}`}
                  >
                    <div className="rough-book-card rounded-xl bg-white">
                      <RoughFrame
                        className="rough-overlay rough-card-line rough-book-cover-line"
                        variant="card"
                      />
                      <div
                        className={`flex aspect-[2/3] w-full items-center justify-center overflow-hidden ${
                          recommendBook.imgUrl ? "" : "book-cover-placeholder"
                        }`}
                      >
                        {recommendBook.imgUrl ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={recommendBook.imgUrl}
                            alt={recommendBook.title}
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <span className="text-sm text-gray-400">
                            표지 없음
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="min-w-0 px-1">
                      <div className="truncate text-sm font-semibold leading-snug">
                        {recommendBook.title}
                      </div>
                      <div className="mt-1 flex items-center gap-1 text-xs theme-muted">
                        <span className="relative inline-block h-3.5 w-3.5 shrink-0">
                          <RoughStarIcon
                            fill={ratingFillColor(recommendBook.averageRating)}
                            className="rough-overlay"
                          />
                        </span>
                        <span>{recommendBook.averageRating.toFixed(1)}</span>
                      </div>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div>
        <div className="flex flex-wrap items-center gap-2">
          <h2 className="text-lg font-bold">리뷰 {reviews.length}개</h2>
          <RoughButton
            roughSize="sm"
            tone="history"
            type="button"
            onClick={handleOpenReviewAction}
          >
            <ReviewIcon />
            리뷰 작성하기
          </RoughButton>
        </div>

        {reviews.length === 0 && (
          <div className="mt-2 text-sm theme-muted">아직 리뷰가 없습니다.</div>
        )}

        <ul className="mt-2 flex w-full flex-col">
          {reviews.map((review) => (
            <li key={review.id} className="relative py-3">
              {editingReviewId === review.id ? (
                <form
                  className="flex flex-col gap-2"
                  onSubmit={(e) => handleEditSubmit(e, review.id)}
                >
                  <RoughRatingInput
                    name="rating"
                    defaultValue={review.rating}
                    label="평점"
                  />
                  <RoughTextarea
                    name="content"
                    defaultValue={review.content}
                    maxLength={30}
                    rows={2}
                  />
                  <RoughInput
                    inputClassName="px-2"
                    type="text"
                    name="tags"
                    defaultValue={review.tags.join(", ")}
                  />
                  <div className="flex gap-2">
                    <RoughButton roughSize="sm" tone="submit" type="submit">
                      수정 완료
                    </RoughButton>
                    <RoughButton
                      roughSize="sm"
                      tone="cancel"
                      type="button"
                      onClick={() => setEditingReviewId(null)}
                    >
                      취소
                    </RoughButton>
                  </div>
                </form>
              ) : (
                <div className="flex items-start gap-3">
                  <Link href={`/members/detail?id=${review.reviewer.id}`}>
                    <Avatar label={review.reviewer.githubId} />
                  </Link>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1">
                      <Link
                        className="font-semibold hover:underline"
                        href={`/members/detail?id=${review.reviewer.id}`}
                      >
                        {review.reviewer.githubId ?? "탈퇴한 사용자"}
                      </Link>
                      {review.reviewer.githubLink && (
                        <a
                          className="rough-github-inline"
                          href={review.reviewer.githubLink}
                          target="_blank"
                          rel="noreferrer"
                          aria-label={`${review.reviewer.githubId ?? "사용자"} GitHub`}
                        >
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img
                            className="rough-github-inline-image"
                            src="/github.svg"
                            alt=""
                          />
                        </a>
                      )}
                    </div>

                    <div className="flex flex-wrap gap-1 text-xs theme-tag">
                      {review.tags.map((tag) => (
                        <span key={tag}>#{tag}</span>
                      ))}
                    </div>

                    <div className="mt-1 text-sm">{review.content}</div>
                    <div className="mt-1 text-xs theme-subtle">
                      {review.createdDate}
                    </div>

                    {loginMember?.id === review.reviewer.id && (
                      <div className="flex gap-2 mt-1">
                        <RoughButton
                          className="px-2"
                          roughSize="sm"
                          type="button"
                          onClick={() => setEditingReviewId(review.id)}
                        >
                          수정
                        </RoughButton>
                        <RoughButton
                          className="px-2"
                          roughSize="sm"
                          tone="cancel"
                          type="button"
                          onClick={() => handleDelete(review.id)}
                        >
                          삭제
                        </RoughButton>
                      </div>
                    )}
                  </div>

                  <span
                    className={`font-bold shrink-0 ${ratingColor(review.rating)}`}
                  >
                    <RatingValue rating={review.rating} />
                  </span>
                </div>
              )}
              <RoughDivider />
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<div>로딩중...</div>}>
      <BookDetail />
    </Suspense>
  );
}

"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { ratingColor } from "@/lib/ratingColor";

import LibraryProfilePanel from "@/app/_components/LibraryProfilePanel";
import LibraryWidgetPreview from "@/app/_components/LibraryWidgetPreview";
import RatingValue from "@/app/_components/RatingValue";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";

type MemberDto = components["schemas"]["MemberDto"];
type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];
type ReviewWithBookImgUrl = NonNullable<
  ReviewsByMemberDto["results"]
>[number] & {
  bookImgUrl?: string | null;
};

function MemberDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const [member, setMember] = useState<MemberDto | null>(null);
  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);

  useEffect(() => {
    if (id == null) {
      goToErrorPage({ message: "회원 ID가 없습니다." });
      return;
    }

    apiFetch(`/api/v1/members/${id}`)
      .then((data) => {
        setMember(data);
      })
      .catch(goToErrorPage);

    apiFetch(`/api/v1/reviews/member/${id}`)
      .then((data: ReviewsByMemberDto) => {
        setReviewData(data);
      })
      .catch(goToErrorPage);
  }, [id]);

  if (id == null) {
    return <div>로딩중...</div>;
  }

  if (member == null || reviewData == null) return <div>로딩중...</div>;

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;
  const reviewResults = reviewData.results ?? [];

  return (
    <div className="flex gap-8">
      <LibraryProfilePanel
        avatarLabel={member.githubId}
        username={member.githubId}
        githubId={member.githubId}
        githubLink={member.githubLink}
        averageLabel="평균 별점"
        averageRating={averageNumber}
        rating={reviewData.rating}
      />

      <div className="flex-1 flex flex-col gap-4">
        {member.githubId && (
          <LibraryWidgetPreview
            githubId={member.githubId}
            widgetSrc={`${API_BASE_URL}/api/v1/widgets/${member.githubId}`}
          />
        )}

        <div>
          <div className="flex gap-2">
            <div className="theme-tab">
              <div className="theme-tab-active px-3 py-2 text-sm">
                작성한 리뷰 {reviewResults.length}
              </div>
            </div>
          </div>

          {reviewResults.length === 0 && (
            <div className="mt-2 text-sm theme-muted">
              작성한 리뷰가 없습니다.
            </div>
          )}

          <ul className="flex w-full flex-col">
            {reviewResults.map((review) => (
              <li
                key={review.id ?? review.bookId}
                className="relative flex items-start gap-3 py-3"
              >
                <RoughDivider fullWidth />
                <Link
                  href={`/books/detail?id=${review.bookId}`}
                  className="rough-book-card review-book-thumbnail relative flex h-20 w-14 shrink-0 items-center justify-center overflow-hidden rounded-lg bg-white"
                  aria-label={`${review.bookTitle ?? `책 #${review.bookId}`} 상세 보기`}
                >
                  <RoughFrame
                    className="rough-overlay rough-card-line rough-book-cover-line"
                    variant="card"
                  />
                  {(review as ReviewWithBookImgUrl).bookImgUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={(review as ReviewWithBookImgUrl).bookImgUrl ?? ""}
                      alt=""
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <span className="text-xs text-gray-400">표지 없음</span>
                  )}
                </Link>
                <div className="flex-1 min-w-0">
                  <Link
                    className="font-semibold hover:underline"
                    href={`/books/detail?id=${review.bookId}`}
                  >
                    {review.bookTitle ?? `책 #${review.bookId}`}
                  </Link>

                  <div className="flex flex-wrap gap-1 text-xs theme-tag">
                    {(review.tags ?? []).map((tag) => (
                      <span key={tag}>#{tag}</span>
                    ))}
                  </div>

                  <div className="text-sm mt-1">{review.content}</div>
                  <div className="mt-1 text-xs theme-subtle">
                    {review.createdDate}
                  </div>
                </div>

                <span
                  className={`font-bold shrink-0 ${
                    typeof review.rating === "number"
                      ? ratingColor(review.rating)
                      : ""
                  }`}
                >
                  <RatingValue rating={review.rating ?? 0} />
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}
export default function Page() {
  return (
    <Suspense fallback={<div>로딩중...</div>}>
      <MemberDetail />
    </Suspense>
  );
}

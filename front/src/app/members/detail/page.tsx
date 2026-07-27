"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { ratingColor } from "@/lib/ratingColor";

import Avatar from "@/app/_components/Avatar";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";

type MemberDto = components["schemas"]["MemberDto"];
type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];

function MemberDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const [member, setMember] = useState<MemberDto | null>(null);
  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);
  const [bookTitles, setBookTitles] = useState<Record<number, string>>({});
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    if (id == null) return;

    apiFetch(`/api/v1/members/${id}`)
      .then((data) => {
        setLoadError(null);
        setMember(data);
      })
      .catch((error) => {
        setLoadError(`${error.resultCode} : ${error.message}`);
      });

    apiFetch(`/api/v1/reviews/member/${id}`)
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
  }, [id]);

  if (id == null) {
    return <div>오류가 발생했습니다: 회원 ID가 없습니다.</div>;
  }

  if (loadError != null) {
    return (
      <div>오류가 발생했습니다: {loadError} (백엔드 확인이 필요합니다)</div>
    );
  }

  if (member == null || reviewData == null) return <div>로딩중...</div>;

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;

  return (
    <div className="flex gap-8">
      <div className="flex flex-col items-center gap-1 w-48 shrink-0">
        <Avatar label={member.githubId} size="lg" />
        <div className="mt-2 text-sm font-semibold">{member.githubId}</div>
        {member.githubLink && (
          <a
            className="theme-link text-sm underline"
            href={member.githubLink}
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
        <div className="text-xs theme-muted">
          {member.githubId}님이 준 평균 별점
        </div>
        <RatingHistogram rating={reviewData.rating} className="mt-3 w-full" />
      </div>

      <div className="flex-1 flex flex-col gap-4">
        {member.githubId && (
          <div>
            <h2 className="font-bold">위젯 미리보기</h2>
            <div className="rough-panel-border mt-1 bg-transparent p-2">
              <RoughFrame className="rough-overlay" variant="card" />
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={`${API_BASE_URL}/api/v1/widgets/${member.githubId}`}
                alt="위젯 미리보기"
              />
            </div>
          </div>
        )}

        <div>
          <div className="theme-tab inline-flex">
            <h2 className="px-3 py-2 text-sm font-bold">
              작성한 리뷰 {reviewData.results.length}
            </h2>
            <RoughDivider
              className="theme-tab-divider"
              color="var(--line)"
              emphasis
              strokeWidth={1.35}
            />
          </div>

          {reviewData.results.length === 0 && (
            <div className="mt-2 text-sm theme-muted">
              작성한 리뷰가 없습니다.
            </div>
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

                  <div className="mt-1 text-sm">{review.content}</div>
                  <div className="mt-1 text-xs theme-subtle">
                    {review.createdDate}
                  </div>
                </div>

                <span
                  className={`font-bold shrink-0 ${ratingColor(review.rating)}`}
                >
                  <RatingValue rating={review.rating} />
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

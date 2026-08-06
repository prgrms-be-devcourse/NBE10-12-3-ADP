"use client";

import Link from "next/link";

import type { components } from "@/lib/backend/apiV1/schema";
import { useAuth } from "@/lib/auth/AuthProvider";
import { formatDateTime } from "@/lib/formatDate";
import { ratingColor } from "@/lib/ratingColor";

import Avatar from "@/app/_components/Avatar";
import RatingValue from "@/app/_components/RatingValue";
import RoughButton from "@/app/_components/RoughButton";

type ReviewDto = components["schemas"]["ReviewDto"];

type ReviewDetailCardProps = {
  review: ReviewDto;
  memberLink?: string | null;
  className?: string;
  showActions?: boolean;
  onToggleLike?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  liked?: boolean;
  likeLabel?: string;
};

export default function ReviewDetailCard({
  review,
  memberLink,
  className = "",
  showActions = false,
  onToggleLike,
  onEdit,
  onDelete,
  liked = false,
  likeLabel = "좋아요",
}: ReviewDetailCardProps) {
  const { loginMember } = useAuth();
  const isMine = loginMember?.id != null && loginMember.id === review.reviewer?.id;
  const reviewerHref = isMine
    ? "/mypage"
    : memberLink ?? (review.reviewer?.id != null ? `/members/detail?id=${review.reviewer.id}` : null);

  return (
    <div className={`flex flex-col gap-2 ${className}`}>
      <div className="flex items-center gap-3">
        {review.reviewer?.id != null ? (
          <Link href={reviewerHref ?? "/mypage"}>
            <Avatar label={review.reviewer.githubId ?? null} />
          </Link>
        ) : (
          <Avatar label={review.reviewer?.githubId ?? null} />
        )}

        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <div className="flex items-center gap-1">
            {review.reviewer?.id != null ? (
              <Link className="font-semibold hover:underline" href={reviewerHref ?? "/mypage"}>
                {review.reviewer.githubId ?? "탈퇴한 사용자"}
              </Link>
            ) : (
              <span className="font-semibold">
                {review.reviewer?.githubId ?? "탈퇴한 사용자"}
              </span>
            )}
            {review.reviewer?.githubId && (
              <a
                className="rough-github-inline"
                href={`https://github.com/${review.reviewer.githubId}`}
                target="_blank"
                rel="noreferrer"
                aria-label={`${review.reviewer.githubId} GitHub`}
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img className="rough-github-inline-image" src="/github.svg" alt="" />
              </a>
            )}
          </div>

          <div className="flex flex-wrap gap-1 text-xs theme-tag">
            {(review.tags ?? []).map((tag) => (
              <span key={tag}>#{tag}</span>
            ))}
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-2">
          <span
            className={`font-bold ${
              typeof review.rating === "number" ? ratingColor(review.rating) : ""
            }`}
          >
            <RatingValue rating={review.rating ?? 0} />
          </span>
        </div>
      </div>

      <div className="flex items-start gap-3">
        <div className="min-w-0 flex-1">
          <div className="text-sm">{review.content}</div>
          <div className="text-xs theme-subtle">{formatDateTime(review.createdDate)}</div>
        </div>

        {showActions && (
          <div className="ml-auto flex shrink-0 flex-col items-end gap-2">
            <div className="flex flex-wrap justify-end gap-2">
              {onEdit && (
                <RoughButton
                  className="px-2"
                  roughSize="sm"
                  type="button"
                  onClick={onEdit}
                >
                  수정
                </RoughButton>
              )}
              {onDelete && (
                <RoughButton
                  className="px-2"
                  roughSize="sm"
                  tone="cancel"
                  type="button"
                  onClick={onDelete}
                >
                  삭제
                </RoughButton>
              )}
              {onToggleLike && (
                <RoughButton
                  className="px-2"
                  roughSize="sm"
                  tone={liked ? "wishActive" : "wish"}
                  type="button"
                  onClick={onToggleLike}
                >
                  {likeLabel}
                </RoughButton>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

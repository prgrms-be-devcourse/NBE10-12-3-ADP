"use client";

import type { ReactNode } from "react";

import { ratingColor } from "@/lib/ratingColor";

import Avatar from "@/app/_components/Avatar";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";

type LibraryProfilePanelProps = {
  avatarLabel: string | null | undefined;
  username: string | null | undefined;
  githubId: string | null | undefined;
  githubLink?: string | null;
  averageRating: number | null;
  averageLabel: string;
  rating?: Record<string, unknown>;
  actions?: ReactNode;
};

export default function LibraryProfilePanel({
  avatarLabel,
  username,
  githubId,
  githubLink,
  averageRating,
  averageLabel,
  rating,
  actions,
}: LibraryProfilePanelProps) {
  return (
    <aside className="flex w-48 shrink-0 flex-col items-center gap-1 pt-6">
      <Avatar label={avatarLabel ?? null} size="lg" />
      <div className="mt-6 w-full text-left text-sm">아이디 : {username}</div>
      <div className="w-full text-left text-sm">닉네임 : {githubId}</div>
      {githubLink && (
        <a
          className="theme-link w-full text-left text-sm underline"
          href={githubLink}
          target="_blank"
          rel="noreferrer"
        >
          github 주소
        </a>
      )}

      <div className="mt-2 flex w-full items-center justify-between gap-2">
        <div className="text-xs theme-muted">{averageLabel}</div>
        {averageRating != null && (
          <div className={`text-lg font-bold ${ratingColor(averageRating)}`}>
            <RatingValue rating={averageRating} />
          </div>
        )}
      </div>
      <RatingHistogram rating={rating ?? {}} className="mt-3 w-full" />
      {actions}
    </aside>
  );
}

"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

type BookThumbnailProps = {
  bookId?: number;
  imgUrl?: string | null;
  title?: string | null;
  className?: string;
  placeholderClassName?: string;
  placeholderText?: string;
  altClassName?: string;
  imgClassName?: string;
  onLoad?: () => void;
};

export default function BookThumbnail({
  bookId,
  imgUrl,
  title,
  className,
  placeholderClassName,
  placeholderText = "표지 없음",
  altClassName,
  imgClassName,
  onLoad,
}: BookThumbnailProps) {
  const normalizedImgUrl = imgUrl?.trim() || null;
  const [thumbnailUrl, setThumbnailUrl] = useState<string | null>(normalizedImgUrl);

  useEffect(() => {
    setThumbnailUrl(normalizedImgUrl);

    if (bookId == null || normalizedImgUrl != null) {
      return;
    }

    let cancelled = false;

    apiFetch(`/api/v1/books/${bookId}/thumbnail`)
      .then((data) => {
        if (cancelled) return;

        if (typeof data === "string" && data.trim() !== "") {
          setThumbnailUrl(data);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setThumbnailUrl(null);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [bookId, normalizedImgUrl]);

  if (thumbnailUrl == null) {
    return (
      <div className={placeholderClassName ?? className}>
        <span className={altClassName ?? "text-sm text-gray-400"}>
          {placeholderText}
        </span>
      </div>
    );
  }

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={thumbnailUrl}
      alt={title ?? ""}
      className={imgClassName ?? className}
      onLoad={onLoad}
    />
  );
}

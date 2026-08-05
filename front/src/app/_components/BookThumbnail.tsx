"use client";

import { type RefObject, useEffect, useState } from "react";

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
  imgRef?: RefObject<HTMLImageElement | null>;
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
  imgRef,
  onLoad,
}: BookThumbnailProps) {
  const normalizedImgUrl = imgUrl?.trim() || null;
  const [thumbnailUrl, setThumbnailUrl] = useState<string | null>(normalizedImgUrl);

  useEffect(() => {
    setThumbnailUrl(normalizedImgUrl);

    if (bookId == null || normalizedImgUrl != null) {
      return;
    }

    apiFetch(`/api/v1/books/${bookId}/thumbnail`)
      .then((data) => {
        if (data?.imgUrl && data.imgUrl.trim() !== "") {
          setThumbnailUrl(data.imgUrl);
        }
      })
      .catch(() => {
        // 실패 시 아무 동작도 하지 않는다 (placeholder 유지)
      });
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
      ref={imgRef}
      src={thumbnailUrl}
      alt={title ?? ""}
      className={imgClassName ?? className}
      onLoad={onLoad}
    />
  );
}

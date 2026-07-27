"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";

import BookGrid from "@/app/_components/BookGrid";

type BookDto = components["schemas"]["BookDto"];

export default function Page() {
  const [popularBooks, setPopularBooks] = useState<BookDto[] | null>(null);
  const [topRatedBooks, setTopRatedBooks] = useState<BookDto[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      apiFetch(`/api/v1/books/rank?type=reviewCount&page=0&size=10`),
      apiFetch(`/api/v1/books/rank?type=rating&page=0&size=10`),
    ])
      .then(([popularData, topRatedData]) => {
        setLoadError(null);
        setPopularBooks(popularData);
        setTopRatedBooks(topRatedData);
      })
      .catch((error) => {
        setLoadError(`${error.resultCode} : ${error.message}`);
      });
  }, []);

  if (loadError != null) {
    return (
      <div>오류가 발생했습니다: {loadError} (백엔드 확인이 필요합니다)</div>
    );
  }

  return (
    <>
      <h1 className="mt-4 mb-2 text-3xl font-bold">인기 도서</h1>
      <BookGrid
        books={popularBooks ?? undefined}
        isLoading={popularBooks == null}
        layout="horizontal"
      />

      <h1 className="mt-8 mb-2 text-3xl font-bold">후기 좋은 도서</h1>
      <BookGrid
        books={topRatedBooks ?? undefined}
        isLoading={topRatedBooks == null}
        layout="horizontal"
      />
    </>
  );
}

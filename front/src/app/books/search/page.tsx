"use client";

import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";

import BookGrid from "@/app/_components/BookGrid";

type BookDto = components["schemas"]["BookDto"];

function SearchResults() {
  const searchParams = useSearchParams();
  const searchTerm = searchParams.get("searchTerm") ?? "";

  const [books, setBooks] = useState<BookDto[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    if (searchTerm.trim().length === 0) {
      setBooks(null);
      setLoadError(null);
      return;
    }

    setBooks(null);
    setLoadError(null);

    apiFetch(
      `/api/v1/books/search?searchTerm=${encodeURIComponent(searchTerm)}`,
    )
      .then((data) => {
        setBooks(data);
      })
      .catch((error) => {
        setLoadError(`${error.resultCode} : ${error.message}`);
      });
  }, [searchTerm]);

  if (searchTerm.trim().length === 0) {
    return <div>검색어를 입력해주세요.</div>;
  }

  if (loadError != null) {
    return (
      <div>오류가 발생했습니다: {loadError} (백엔드 확인이 필요합니다)</div>
    );
  }

  if (books == null) return <div>검색중...</div>;

  return (
    <>
      <h1>&apos;{searchTerm}&apos; 검색 결과</h1>
      <BookGrid books={books} />
    </>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<div>검색중...</div>}>
      <SearchResults />
    </Suspense>
  );
}

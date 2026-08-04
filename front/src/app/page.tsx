"use client";

import Link from "next/link";

import {
  Fragment,
  type ReactNode,
  useEffect,
  useRef,
  useState,
} from "react";

import { apiFetch } from "@/lib/backend/client";
import { goToErrorPage } from "@/lib/error/goToErrorPage";

import type { components } from "@/lib/backend/apiV1/schema";

import BookGrid from "@/app/_components/BookGrid";
import RatingValue from "@/app/_components/RatingValue";
import RoughBar from "@/app/_components/RoughBar";
import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";

type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"];
type LatestReviewDto = ReviewDto & {
  bookImgUrl?: string | null;
};

function CarouselArrow({ direction }: { direction: "left" | "right" }) {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {direction === "left" ? (
        <path d="M15 18l-6-6 6-6" />
      ) : (
        <path d="M9 18l6-6-6-6" />
      )}
    </svg>
  );
}

function FireIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 22c4 0 7-2.8 7-6.7 0-2.3-1-4.2-2.9-5.9.1 1.9-.8 3.2-2.2 3.8.4-3.8-1.4-6.8-5-9.2.3 3-1.3 4.9-2.6 6.6A7.2 7.2 0 0 0 5 15.3C5 19.2 8 22 12 22Z" />
      <path d="M10 17c0-1.4.8-2.4 2.1-3.4.8 1.1 1.9 2.2 1.9 3.7A2 2 0 0 1 12 19a2 2 0 0 1-2-2Z" />
    </svg>
  );
}

function SectionStarIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m12 3 2.7 5.5 6 .9-4.3 4.2 1 6-5.4-2.9-5.4 2.9 1-6-4.3-4.2 6-.9L12 3Z" />
    </svg>
  );
}

function StackedBooksIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M4 6.5 15.5 4 20 6.5v13L8.5 22 4 19.5v-13Z" />
      <path d="M8.5 9 20 6.5" />
      <path d="M8.5 22V9L4 6.5" />
      <path d="M12 6.7v13" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="8" />
      <path d="M12 8v4l2.8 1.6" />
    </svg>
  );
}

function MainBookSection({
  title,
  icon,
  books,
  className,
}: {
  title: string;
  icon: ReactNode;
  books: BookDto[] | null;
  className: string;
}) {
  const scrollRef = useRef<HTMLUListElement | null>(null);

  if (books != null && books.length === 0) {
    return null;
  }

  const moveCarousel = (direction: -1 | 1) => {
    const scrollElement = scrollRef.current;
    if (scrollElement == null) return;

    const bookItems = scrollElement.querySelectorAll<HTMLElement>(
      "[data-book-item='true']",
    );
    const scrollDistance =
      bookItems[1] != null
        ? bookItems[1].offsetLeft - bookItems[0].offsetLeft
        : (bookItems[0]?.getBoundingClientRect().width ?? 0);

    scrollElement.scrollBy({
      left: scrollDistance * 2 * direction,
      behavior: "smooth",
    });
  };

  return (
    <section className={className}>
      <div className="mb-2 flex w-full items-center gap-3">
        <h1 className="main-section-heading text-2xl font-bold">
          <span className="shrink-0" aria-hidden="true">
            {icon}
          </span>
          <span>{title}</span>
        </h1>
        <div className="ml-auto flex shrink-0 items-center gap-1">
          <RoughButton
            type="button"
            className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
            roughSize="sm"
            onClick={() => moveCarousel(-1)}
            aria-label={`${title} 이전 목록`}
          >
            <CarouselArrow direction="left" />
          </RoughButton>
          <RoughButton
            type="button"
            className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
            roughSize="sm"
            onClick={() => moveCarousel(1)}
            aria-label={`${title} 다음 목록`}
          >
            <CarouselArrow direction="right" />
          </RoughButton>
        </div>
      </div>
      <BookGrid
        books={books ?? undefined}
        isLoading={books == null}
        layout="horizontal"
        scrollRef={scrollRef}
      />
    </section>
  );
}

function LatestReviewSection({
  reviews,
}: {
  reviews: LatestReviewDto[] | null;
}) {
  const scrollRef = useRef<HTMLUListElement | null>(null);

  if (reviews != null && reviews.length === 0) {
    return null;
  }

  const moveCarousel = (direction: -1 | 1) => {
    const scrollElement = scrollRef.current;
    if (scrollElement == null) return;

    const reviewItems = scrollElement.querySelectorAll<HTMLElement>(
      "[data-review-item='true']",
    );
    const scrollDistance =
      reviewItems[1] != null
        ? reviewItems[1].offsetLeft - reviewItems[0].offsetLeft
        : (reviewItems[0]?.getBoundingClientRect().width ?? 0);
    const scrollStep = window.matchMedia("(min-width: 640px)").matches ? 2 : 1;

    scrollElement.scrollBy({
      left: scrollDistance * scrollStep * direction,
      behavior: "smooth",
    });
  };

  return (
    <section className="mt-6">
      <div className="mb-2 flex w-full items-center gap-3">
        <h1 className="main-section-heading text-2xl font-bold">
          <span className="shrink-0" aria-hidden="true">
            <ClockIcon />
          </span>
          <span>최근 리뷰</span>
        </h1>
        <div className="ml-auto flex shrink-0 items-center gap-1">
          <RoughButton
            type="button"
            className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
            roughSize="sm"
            onClick={() => moveCarousel(-1)}
            aria-label="최근 리뷰 이전 목록"
          >
            <CarouselArrow direction="left" />
          </RoughButton>
          <RoughButton
            type="button"
            className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
            roughSize="sm"
            onClick={() => moveCarousel(1)}
            aria-label="최근 리뷰 다음 목록"
          >
            <CarouselArrow direction="right" />
          </RoughButton>
        </div>
      </div>
      <div className="book-scroll-fade">
        <ul
          ref={scrollRef}
          className="book-scroll-list flex gap-4 overflow-x-auto py-2 pb-4"
        >
          {reviews == null
            ? Array.from({ length: 4 }).map((_, index) => (
                <li
                  key={index}
                  className="latest-review-item shrink-0"
                  aria-hidden="true"
                >
                  <div className="book-skeleton h-32 rounded-lg" />
                </li>
              ))
            : reviews.map((review) => (
                <li
                  key={review.id}
                  className="latest-review-item shrink-0"
                  data-review-item="true"
                >
                  <article className="rough-panel-border relative flex h-full gap-3 bg-[var(--surface)] p-3">
                    <RoughFrame className="rough-overlay" variant="card" />
                    <Link
                      href={`/books/detail?id=${review.bookId}`}
                      className="rough-book-card review-book-thumbnail relative flex h-28 w-20 shrink-0 items-center justify-center overflow-hidden rounded-lg bg-white"
                      aria-label={`${review.bookTitle ?? `책 #${review.bookId}`} 상세 보기`}
                    >
                      <RoughFrame
                        className="rough-overlay rough-card-line rough-book-cover-line"
                        variant="card"
                      />
                      {review.bookImgUrl ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={review.bookImgUrl}
                          alt=""
                          className="relative z-0 h-full w-full object-cover"
                        />
                      ) : (
                        <span className="text-xs text-gray-400">표지 없음</span>
                      )}
                    </Link>
                    <div className="flex min-w-0 flex-1 flex-col">
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                          <Link
                            href={`/books/detail?id=${review.bookId}`}
                            className="theme-link font-bold"
                          >
                            {review.bookTitle ?? `책 #${review.bookId}`}
                          </Link>
                          <div className="text-xs theme-muted">
                            {review.reviewer?.githubId ?? "익명"} ·{" "}
                            {review.createdDate?.slice(0, 10) ?? ""}
                          </div>
                        </div>
                        <span className="shrink-0 font-bold">
                          <RatingValue rating={review.rating ?? 0} />
                        </span>
                      </div>
                      <p className="mt-2 line-clamp-3 text-sm">
                        {review.content}
                      </p>
                    </div>
                  </article>
                </li>
              ))}
        </ul>
      </div>
    </section>
  );
}

export default function Page() {
  const [popularBooks, setPopularBooks] = useState<BookDto[] | null>(null);
  const [topRatedBooks, setTopRatedBooks] = useState<BookDto[] | null>(null);
  const [mostReviewedBooks, setMostReviewedBooks] = useState<BookDto[] | null>(
    null,
  );
  const [latestReviews, setLatestReviews] = useState<LatestReviewDto[] | null>(
    null,
  );
  useEffect(() => {
    Promise.all([
      apiFetch(`/api/v1/books/rank?type=views&page=0&size=10`),
      apiFetch(`/api/v1/reviews/latest?page=0&size=10`),
      apiFetch(`/api/v1/books/rank?type=rating&page=0&size=10`),
      apiFetch(`/api/v1/books/rank?type=reviewCount&page=0&size=10`),
    ])
      .then(([popularData, latestReviewData, topRatedData, mostReviewedData]) => {
        setPopularBooks(popularData);
        setLatestReviews(latestReviewData);
        setTopRatedBooks(topRatedData);
        setMostReviewedBooks(mostReviewedData);
      })
      .catch(goToErrorPage);
  }, []);

  const rankedSections = [
    {
      title: "평점 좋은 도서",
      icon: <SectionStarIcon />,
      books: topRatedBooks,
    },
    {
      title: "리뷰 많은 도서",
      icon: <StackedBooksIcon />,
      books: mostReviewedBooks,
    },
  ].filter(({ books }) => books == null || books.length > 0);

  return (
    <>
      {(popularBooks == null || popularBooks.length > 0) && (
        <>
          <MainBookSection
            title="인기 도서"
            icon={<FireIcon />}
            books={popularBooks}
            className="mt-4"
          />
          <div className="theme-section-divider-bar mt-7 opacity-25">
            <RoughBar
              className="h-full w-full"
              fill="transparent"
              lineInset={0}
              variant="line"
            />
          </div>
          <LatestReviewSection reviews={latestReviews} />
        </>
      )}

      {rankedSections.map((section, index) => (
        <Fragment key={section.title}>
          {(popularBooks == null || popularBooks.length > 0 || index > 0) && (
            <div className="theme-section-divider-bar mt-7 opacity-25">
              <RoughBar
                className="h-full w-full"
                fill="transparent"
                lineInset={0}
                variant="line"
              />
            </div>
          )}
          <MainBookSection
            title={section.title}
            icon={section.icon}
            books={section.books}
            className="mt-6"
          />
        </Fragment>
      ))}
      {popularBooks != null &&
        popularBooks.length === 0 &&
        rankedSections.length === 0 && (
          <LatestReviewSection reviews={latestReviews} />
      )}
    </>
  );
}

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
import type { components } from "@/lib/backend/apiV1/schema";

import BookGrid from "@/app/_components/BookGrid";
import BookCoverCard from "@/app/_components/BookCoverCard";
import BookThumbnail from "@/app/_components/BookThumbnail";
import RatingValue from "@/app/_components/RatingValue";
import RoughBar from "@/app/_components/RoughBar";
import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";
import ReviewDetailCard from "@/app/_components/ReviewDetailCard";
import LikedReviewCard from "@/app/_components/LikedReviewCard";
import BookTape from "@/app/_components/BookTape";

type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"];
type LatestReviewDto = ReviewDto & {
  bookImgUrl?: string | null;
};
type PopularReviewDto = ReviewDto & {
  bookImgUrl?: string | null;
};

function extractListData<T>(data: unknown): T[] {
  if (Array.isArray(data)) {
    return data;
  }

  if (
    data != null &&
    typeof data === "object" &&
    "content" in data &&
    Array.isArray(data.content)
  ) {
    return data.content as T[];
  }

  return [];
}

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

function PopularReviewSection({
  title,
  icon,
  reviews,
}: {
  title: string;
  icon: ReactNode;
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
    const scrollStep = scrollDistance * 2 <= scrollElement.clientWidth ? 2 : 1;

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
                  className="w-[calc((100%-1rem)/2)] shrink-0 max-[900px]:w-full"
                  data-review-item="true"
                >
                  <LikedReviewCard
                    review={review}
                    className="rounded-2xl bg-[var(--surface)] p-3 shadow-sm"
                    reviewCardClassName="p-0"
                  />
                </li>
              ))}
        </ul>
      </div>
    </section>
  );
}

function LatestReviewBookSection({
  title,
  icon,
  reviews,
}: {
  title: string;
  icon: ReactNode;
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
    const scrollStep = window.matchMedia("(min-width: 640px)").matches ? 3 : 1;

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
      <div className="book-scroll-fade">
        <ul
          ref={scrollRef}
          className="book-scroll-list flex gap-3 overflow-x-auto py-2 pb-4"
        >
          {reviews == null
            ? Array.from({ length: 6 }).map((_, index) => (
                <li
                  key={index}
                  className="shrink-0"
                  aria-hidden="true"
                >
                  <div className="book-skeleton h-36 w-28 rounded-lg" />
                </li>
              ))
            : reviews.map((review) => (
                <li
                  key={review.id}
                  className="w-[calc((100%-3.75rem)/6)] shrink-0"
                  data-review-item="true"
                >
                  <article className="relative h-full w-full overflow-hidden rounded-xl bg-white">
                    <BookCoverCard
                      bookId={review.bookId}
                      imgUrl={review.bookImgUrl}
                      title={review.bookTitle}
                      href={`/books/detail?id=${review.bookId}`}
                      ariaLabel={`${review.bookTitle ?? `책 #${review.bookId}`} 상세 보기`}
                      className="aspect-[2/3] w-full rounded-xl"
                      placeholderClassName="bg-[var(--surface-card)] items-center justify-center text-center"
                      placeholderText="표지 없음"
                    >
                      <BookTape>
                        <div className="flex flex-col gap-1">
                          <div className="line-clamp-1 text-xs font-bold">
                            {review.bookTitle ?? `책 #${review.bookId}`}
                          </div>
                          <span className="text-xs font-bold">
                            <RatingValue rating={review.rating ?? 0} />
                          </span>
                        </div>
                      </BookTape>
                    </BookCoverCard>
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
  const [popularReviews, setPopularReviews] = useState<PopularReviewDto[] | null>(
    null,
  );
  useEffect(() => {
    Promise.allSettled([
      apiFetch(`/api/v1/books/rank?type=views&page=0&size=10`),
      apiFetch(`/api/v1/reviews/latest?page=0&size=10`),
      apiFetch(`/api/v1/reviews/likeCount?page=0&size=10`),
      apiFetch(`/api/v1/books/rank?type=rating&page=0&size=10`),
      apiFetch(`/api/v1/books/rank?type=reviewCount&page=0&size=10`),
    ]).then(([popularData, latestReviewData, popularReviewData, topRatedData, mostReviewedData]) => {
      setPopularBooks(
        popularData.status === "fulfilled"
          ? extractListData<BookDto>(popularData.value)
          : [],
      );
      setLatestReviews(
        latestReviewData.status === "fulfilled"
          ? extractListData<LatestReviewDto>(latestReviewData.value)
          : [],
      );
      setPopularReviews(
        popularReviewData.status === "fulfilled"
          ? extractListData<PopularReviewDto>(popularReviewData.value)
          : [],
      );
      setTopRatedBooks(
        topRatedData.status === "fulfilled"
          ? extractListData<BookDto>(topRatedData.value)
          : [],
      );
      setMostReviewedBooks(
        mostReviewedData.status === "fulfilled"
          ? extractListData<BookDto>(mostReviewedData.value)
          : [],
      );
    });
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
      {[
        popularBooks == null || popularBooks.length > 0
          ? (
              <MainBookSection
                key="popular-books"
                title="인기 도서"
                icon={<FireIcon />}
                books={popularBooks}
                className="mt-4"
              />
            )
          : null,
        popularReviews == null || popularReviews.length > 0
          ? (
              <PopularReviewSection
                key="popular-reviews"
                title="인기 많은 리뷰"
                icon={<FireIcon />}
                reviews={popularReviews}
              />
            )
          : null,
        latestReviews == null || latestReviews.length > 0
          ? (
              <LatestReviewBookSection
                key="latest-reviews"
                title="최근 리뷰가 추가된 도서"
                icon={<ClockIcon />}
                reviews={latestReviews}
              />
            )
          : null,
        ...rankedSections.map((section) => (
          <MainBookSection
            key={section.title}
            title={section.title}
            icon={section.icon}
            books={section.books}
            className="mt-6"
          />
        )),
      ]
        .filter(Boolean)
        .map((section, index) => (
          <Fragment key={index}>
            {index > 0 && (
              <div className="theme-section-divider-bar mt-7 opacity-25">
                <RoughBar
                  className="h-full w-full"
                  fill="transparent"
                  lineInset={0}
                  variant="line"
                />
              </div>
            )}
            {section}
          </Fragment>
        ))}
    </>
  );
}

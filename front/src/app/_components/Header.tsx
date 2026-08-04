"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useEffect, useRef, useState } from "react";

import { useAuth } from "@/lib/auth/AuthProvider";
import {
  buildGitHubLoginUrl,
  consumeAuthReturnPath,
  saveCurrentAuthReturnPath,
} from "@/lib/auth/authReturnPath";
import { useTheme } from "@/lib/theme/ThemeProvider";
import { useToast } from "@/lib/toast/ToastProvider";

import RoughBar from "@/app/_components/RoughBar";
import RoughButton from "@/app/_components/RoughButton";
import { RoughInput } from "@/app/_components/RoughInput";

function LibraryIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M4 19.5V5a2 2 0 0 1 2-2h12" />
      <path d="M6 17h12" />
      <path d="M6 21h12a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2" />
    </svg>
  );
}

function LogoutIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="M16 17l5-5-5-5" />
      <path d="M21 12H9" />
    </svg>
  );
}

function SearchIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="11" cy="11" r="7" />
      <path d="M20 20l-3.5-3.5" />
    </svg>
  );
}

function AdminIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 3l7 4v5c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V7l7-4Z" />
      <path d="M9.5 12l1.7 1.7 3.6-3.7" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg
      aria-hidden="true"
      className="theme-light-only h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M20.5 14.5A8.5 8.5 0 0 1 9.5 3.5a7 7 0 1 0 11 11Z" />
    </svg>
  );
}

function SunIcon() {
  return (
    <svg
      aria-hidden="true"
      className="theme-dark-only h-4 w-4"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2" />
      <path d="M12 20v2" />
      <path d="M4.93 4.93l1.41 1.41" />
      <path d="M17.66 17.66l1.41 1.41" />
      <path d="M2 12h2" />
      <path d="M20 12h2" />
      <path d="M4.93 19.07l1.41-1.41" />
      <path d="M17.66 6.34l1.41-1.41" />
    </svg>
  );
}

function GitHubIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M12 2C6.48 2 2 6.58 2 12.22c0 4.52 2.87 8.35 6.84 9.71.5.09.68-.22.68-.49 0-.24-.01-.88-.01-1.73-2.78.62-3.37-1.37-3.37-1.37-.45-1.18-1.11-1.5-1.11-1.5-.91-.63.07-.62.07-.62 1 .07 1.53 1.05 1.53 1.05.89 1.56 2.34 1.11 2.91.85.09-.66.35-1.11.63-1.36-2.22-.26-4.56-1.14-4.56-5.06 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.3.1-2.71 0 0 .84-.27 2.75 1.05A9.33 9.33 0 0 1 12 6.95c.85 0 1.7.12 2.5.34 1.9-1.32 2.74-1.05 2.74-1.05.55 1.41.2 2.45.1 2.71.64.72 1.03 1.63 1.03 2.75 0 3.93-2.34 4.8-4.57 5.05.36.32.68.94.68 1.9 0 1.37-.01 2.47-.01 2.81 0 .27.18.59.69.49A10.08 10.08 0 0 0 22 12.22C22 6.58 17.52 2 12 2Z" />
    </svg>
  );
}

export default function Header() {
  const router = useRouter();
  const { isLogin, isLoginMemberPending, isAdmin, logout } = useAuth();
  const { showErrorToast } = useToast();
  const { toggleTheme } = useTheme();
  const [searchTerm, setSearchTerm] = useState("");
  const [isSearchPending, setIsSearchPending] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const searchInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    const handleSearchLoading = (event: Event) => {
      setIsSearchPending(Boolean((event as CustomEvent<boolean>).detail));
    };

    window.addEventListener("readthem-search-loading", handleSearchLoading);

    return () => {
      window.removeEventListener(
        "readthem-search-loading",
        handleSearchLoading,
      );
    };
  }, []);

  useEffect(() => {
    if (!isSearchOpen) return;

    const frameId = window.requestAnimationFrame(() => {
      searchInputRef.current?.focus();
    });

    return () => {
      window.cancelAnimationFrame(frameId);
    };
  }, [isSearchOpen]);

  useEffect(() => {
    const mediaQuery = window.matchMedia("(min-width: 521px)");
    const closeMobileSearch = () => {
      if (mediaQuery.matches) {
        setIsSearchOpen(false);
      }
    };

    closeMobileSearch();
    mediaQuery.addEventListener("change", closeMobileSearch);

    return () => {
      mediaQuery.removeEventListener("change", closeMobileSearch);
    };
  }, []);

  const handleLogout = () => {
    saveCurrentAuthReturnPath();

    logout()
      .then(() => {
        router.replace(consumeAuthReturnPath());
      })
      .catch(showErrorToast);
  };

  const handleGitHubLogin = (
    e: React.MouseEvent<HTMLAnchorElement, MouseEvent>,
  ) => {
    e.preventDefault();
    window.location.href = buildGitHubLoginUrl();
  };

  const handleSearch = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const term = searchTerm.trim();
    if (term.length === 0 || isSearchPending) return;

    const currentUrl = new URL(window.location.href);
    const currentSearchTerm = currentUrl.searchParams.get("searchTerm") ?? "";
    if (
      currentUrl.pathname === "/books/search" &&
      currentSearchTerm.trim() === term
    ) {
      return;
    }

    setIsSearchPending(true);
    router.push(`/books/search?searchTerm=${encodeURIComponent(term)}`);
  };

  const closeMobileSearchOnBlur = (e: React.FocusEvent<HTMLFormElement>) => {
    if (!isSearchOpen) return;
    if (e.currentTarget.contains(e.relatedTarget)) return;
    if (!window.matchMedia("(max-width: 520px)").matches) return;

    setIsSearchOpen(false);
  };

  return (
    <header className="theme-site-header relative">
      <nav className="mx-auto flex w-full max-w-4xl items-center gap-3 px-4 py-2">
        <div className={`shrink-0 ${isSearchOpen ? "max-[520px]:hidden" : ""}`}>
          <Link
            href="/"
            className="shrink-0 text-xl font-bold whitespace-nowrap"
          >
            READTHEM.md
          </Link>
        </div>

        {!isSearchOpen && (
          <button
            type="button"
            className="mobile-search-toggle theme-nav-link theme-nav-control shrink-0"
            onClick={() => setIsSearchOpen(true)}
            aria-label="검색창 열기"
          >
            <SearchIcon />
            <span className="theme-nav-label">검색</span>
          </button>
        )}

        <form
          className={`min-w-0 items-center gap-1 ${
            isSearchOpen
              ? "flex flex-1"
              : "hidden w-72 shrink-0 min-[521px]:flex lg:w-80"
          }`}
          onSubmit={handleSearch}
          onBlur={closeMobileSearchOnBlur}
        >
          <label className="min-w-0 flex-1">
            <RoughInput
              ref={searchInputRef}
              wrapperClassName="w-full"
              type="text"
              placeholder="제목/저자 검색"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              maxLength={100}
              disabled={isSearchPending}
            />
          </label>
          <RoughButton
            className={`search-submit-button theme-nav-control shrink-0 whitespace-nowrap px-2 ${
              isSearchOpen ? "search-submit-button-open" : ""
            }`}
            roughSize="sm"
            type="submit"
            disabled={isSearchPending}
          >
            <SearchIcon />
            <span className="search-submit-label">
              {isSearchPending ? "검색중" : "검색"}
            </span>
          </RoughButton>
        </form>

        <div
          className={`ml-auto shrink-0 items-center justify-end gap-2 whitespace-nowrap ${
            isSearchOpen ? "hidden min-[521px]:flex" : "flex"
          }`}
        >
          <button
            type="button"
            className="theme-nav-link theme-nav-control shrink-0"
            onClick={toggleTheme}
            aria-label="테마 전환"
          >
            <MoonIcon />
            <SunIcon />
            <span className="theme-nav-label theme-toggle-label" />
          </button>

          {isLoginMemberPending && (
            <span
              className="theme-nav-link theme-nav-control pointer-events-none invisible gap-1.5"
              aria-hidden="true"
            >
              <GitHubIcon />
              <span className="theme-nav-label">GitHub로 로그인</span>
            </span>
          )}

          {!isLoginMemberPending && !isLogin && (
            <div className="auth-action-enter">
              <a
                href={`${process.env.NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/github?redirectUrl=${process.env.NEXT_PUBLIC_FRONTEND_BASE_URL}`}
                className="theme-nav-link theme-nav-control gap-1.5"
                onClick={handleGitHubLogin}
                aria-label="GitHub로 로그인"
              >
                <GitHubIcon />
                <span className="theme-nav-label">GitHub로 로그인</span>
              </a>
            </div>
          )}

          {!isLoginMemberPending && isLogin && (
            <div className="auth-action-enter flex min-w-0 items-center gap-2 whitespace-nowrap">
              <Link href="/mypage" className="theme-nav-link theme-nav-control">
                <LibraryIcon />
                <span className="theme-nav-label">내 서재</span>
              </Link>
              {isAdmin && (
                <Link
                  href="/admin"
                  className="theme-nav-link theme-nav-control"
                >
                  <AdminIcon />
                  <span className="theme-nav-label">관리자</span>
                </Link>
              )}
              <button
                type="button"
                className="theme-nav-link theme-nav-control"
                onClick={handleLogout}
              >
                <LogoutIcon />
                <span className="theme-nav-label">로그아웃</span>
              </button>
            </div>
          )}
        </div>
      </nav>
      <div className="theme-rough-divider-bar">
        <RoughBar
          className="h-full w-full"
          fill="transparent"
          lineInset={0}
          variant="line"
        />
      </div>
    </header>
  );
}

"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useState } from "react";

import { useAuth } from "@/lib/auth/AuthProvider";
import { useTheme } from "@/lib/theme/ThemeProvider";

import RoughButton from "@/app/_components/RoughButton";
import RoughDivider from "@/app/_components/RoughDivider";
import { RoughInput } from "@/app/_components/RoughInput";

export default function Header() {
  const router = useRouter();
  const { loginMember, isLogin, isLoginMemberPending, isAdmin, logout } =
    useAuth();
  const { toggleTheme } = useTheme();
  const [searchTerm, setSearchTerm] = useState("");

  const handleLogout = () => {
    logout().then(() => {
      router.replace(`/`);
    });
  };

  const handleSearch = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const term = searchTerm.trim();
    if (term.length === 0) return;

    router.push(`/books/search?searchTerm=${encodeURIComponent(term)}`);
  };

  return (
    <header className="relative">
      <nav className="mx-auto flex w-full max-w-4xl items-center gap-3 px-4 py-2">
        <div className="shrink-0">
          <Link href="/" className="shrink-0 text-xl font-bold whitespace-nowrap">
            READTHEM.md
          </Link>
        </div>

        <form
          className="flex min-w-0 flex-1 items-center gap-1"
          onSubmit={handleSearch}
        >
          <label className="min-w-0 flex-1">
            <RoughInput
              wrapperClassName="w-full"
              type="text"
              placeholder="제목/저자 검색"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              maxLength={100}
            />
          </label>
          <RoughButton
            className="shrink-0 whitespace-nowrap px-2"
            roughSize="sm"
            type="submit"
          >
            검색
          </RoughButton>
        </form>

        <div className="flex shrink-0 items-center justify-end gap-2 whitespace-nowrap">
          <button
            type="button"
            className="theme-nav-button grid w-20 shrink-0 grid-cols-[1.25rem_2.25rem] items-center gap-2"
            onClick={toggleTheme}
            aria-label="테마 전환"
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/moon.svg" alt="" className="theme-light-only h-5 w-5" />
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/sun.svg" alt="" className="theme-dark-only h-5 w-5" />
            <span className="theme-toggle-label text-sm" />
          </button>

          {isLoginMemberPending && (
            <span
              className="rough-github-link pointer-events-none invisible gap-1.5 text-sm"
              aria-hidden="true"
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                className="rough-github-button-icon"
                src="/github.svg"
                alt=""
              />
              <span>GitHub로 로그인</span>
            </span>
          )}

          {!isLoginMemberPending && !isLogin && (
            <div className="auth-action-enter">
              <a
                href={`${process.env.NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/github?redirectUrl=${process.env.NEXT_PUBLIC_FRONTEND_BASE_URL}`}
                className="rough-github-link gap-1.5 text-sm"
                aria-label="GitHub로 로그인"
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  className="rough-github-button-icon"
                  src="/github.svg"
                  alt=""
                />
                <span>GitHub로 로그인</span>
              </a>
            </div>
          )}

          {!isLoginMemberPending && isLogin && (
            <div className="auth-action-enter flex min-w-0 items-center gap-2 whitespace-nowrap">
              <Link
                href="/mypage"
                className="theme-nav-link min-w-0 max-w-[10rem] truncate"
                title={loginMember?.username ? `${loginMember.username}님` : ""}
              >
                {loginMember?.username}님
              </Link>
              {isAdmin && (
                <Link href="/admin" className="theme-nav-link">
                  관리자
                </Link>
              )}
              <button
                type="button"
                className="theme-nav-link"
                onClick={handleLogout}
              >
                로그아웃
              </button>
            </div>
          )}
        </div>
      </nav>
      <div className="mx-auto w-full max-w-4xl px-1">
        <RoughDivider list={false} />
      </div>
    </header>
  );
}

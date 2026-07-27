import type { Metadata } from "next";

import { AuthProvider } from "@/lib/auth/AuthProvider";
import { ThemeProvider } from "@/lib/theme/ThemeProvider";

import Header from "@/app/_components/Header";

import "./globals.css";

export const metadata: Metadata = {
  title: "READTHEM.md",
  description: "스프링부트, Next.js 연동",
};

const themeScript = `
(() => {
  try {
    const storedTheme = window.localStorage.getItem("readthem-theme");
    const theme =
      storedTheme === "light" || storedTheme === "dark"
        ? storedTheme
        : window.matchMedia("(prefers-color-scheme: dark)").matches
          ? "dark"
          : "light";

    document.documentElement.dataset.theme = theme;
  } catch {
    document.documentElement.dataset.theme = "light";
  }
})();
`;

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeScript }} />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          rel="preconnect"
          href="https://fonts.gstatic.com"
          crossOrigin="anonymous"
        />
        {/* eslint-disable-next-line @next/next/no-page-custom-font -- this is the root layout, applies to every page */}
        <link
          href="https://fonts.googleapis.com/css2?family=Gaegu:wght@400;700&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="flex min-h-screen flex-col antialiased">
        <ThemeProvider>
          <AuthProvider>
            <Header />
            <main className="mx-auto flex w-full max-w-4xl flex-grow flex-col p-4">
              <div className="relative flex flex-1 flex-col">{children}</div>
            </main>
            <footer className="px-2 py-10 text-center text-sm theme-muted">
              <a
                href="https://github.com/prgrms-be-devcourse/NBE10-12-2-ADP"
                target="_blank"
                rel="noreferrer"
                className="theme-link"
              >
                GitHub Repository
              </a>
            </footer>
          </AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}

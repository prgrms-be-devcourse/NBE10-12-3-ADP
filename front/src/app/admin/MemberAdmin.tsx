"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";

import RoughButton from "@/app/_components/RoughButton";

type PageAdminMemberDto = components["schemas"]["PageAdminMemberDto"];

export default function MemberAdmin() {
  const [pageData, setPageData] = useState<PageAdminMemberDto | null>(null);
  const [pageNumber, setPageNumber] = useState(0);

  const loadMembers = (page: number) => {
    apiFetch(`/api/v1/members/admin?page=${page}&size=10`).then(setPageData);
  };

  useEffect(() => {
    loadMembers(pageNumber);
  }, [pageNumber]);

  const handleForceDelete = (id: number) => {
    if (!confirm("이 회원을 강제 탈퇴시키겠습니까?")) return;

    apiFetch(`/api/v1/members/admin/${id}`, { method: "DELETE" })
      .then((data) => {
        alert(data.message);
        loadMembers(pageNumber);
      })
      .catch((error) => {
        alert(`${error.resultCode} : ${error.message}`);
      });
  };

  if (pageData == null) return <div>로딩중...</div>;

  const members = pageData.content ?? [];

  return (
    <div className="flex flex-col gap-4">
      <div className="sketch-panel overflow-x-auto p-2">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b">
              <th className="p-2">ID</th>
              <th className="p-2">아이디</th>
              <th className="p-2">닉네임</th>
              <th className="p-2">GitHub</th>
              <th className="p-2">권한</th>
              <th className="p-2">상태</th>
              <th className="p-2">가입일</th>
              <th className="p-2"></th>
            </tr>
          </thead>
          <tbody>
            {members.map((member) => (
              <tr key={member.id} className="border-b">
                <td className="p-2">{member.id}</td>
                <td className="p-2">{member.username}</td>
                <td className="p-2">{member.nickname}</td>
                <td className="p-2">{member.githubId}</td>
                <td className="p-2">{member.isAdmin ? "관리자" : "일반"}</td>
                <td className="p-2 theme-muted">
                  {member.isDeleted ? "탈퇴" : "정상"}
                </td>
                <td className="p-2 theme-subtle">{member.createdDate}</td>
                <td className="p-2">
                  {!member.isAdmin && !member.isDeleted && (
                    <RoughButton
                      roughSize="sm"
                      tone="cancel"
                      type="button"
                      onClick={() => handleForceDelete(member.id)}
                    >
                      강제 탈퇴
                    </RoughButton>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center gap-2">
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.first}
          onClick={() => setPageNumber((p) => Math.max(0, p - 1))}
        >
          이전
        </RoughButton>
        <span className="text-sm">
          {(pageData.number ?? 0) + 1} / {pageData.totalPages ?? 1}
        </span>
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.last}
          onClick={() => setPageNumber((p) => p + 1)}
        >
          다음
        </RoughButton>
      </div>
    </div>
  );
}

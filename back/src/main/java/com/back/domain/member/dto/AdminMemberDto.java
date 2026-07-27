package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AdminMemberDto(
        @NotNull
        Long id,
        @NotNull
        String username,
        String nickname,
        String githubId,
        @NotNull
        Boolean isAdmin,
        @NotNull
        Boolean isDeleted,
        @NotNull
        LocalDateTime createdDate
) {
    public AdminMemberDto(Member member) {
        this(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getGithubId(),
                member.isAdmin(),
                member.isDeleted(),
                member.getCreatedDate()
        );
    }
}
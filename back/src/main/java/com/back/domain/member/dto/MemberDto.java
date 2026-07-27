package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;

public record MemberDto(
        @NotNull
        Long id,
        @NotNull
        String githubId,
        @NotNull
        String githubLink
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getGithubId(),
                member.getGithubLink()
        );
    }
}

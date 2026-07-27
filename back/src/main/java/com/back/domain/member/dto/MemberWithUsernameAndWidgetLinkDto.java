package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;

public record MemberWithUsernameAndWidgetLinkDto(
        @NotNull
        Long id,
        @NotNull
        String username,
        @NotNull
        String githubId,
        @NotNull
        String githubLink,
        @NotNull
        String widgetLink
) {
    public MemberWithUsernameAndWidgetLinkDto(Member member) {
        this(member.getId(),
                member.getUsername(),
                member.getGithubId(),
                member.getGithubLink(),
                member.getWidgetLink());
    }
}

package com.back.domain.member.dto

import com.back.domain.member.entity.Member
import jakarta.validation.constraints.NotNull

data class MemberWithUsernameAndWidgetLinkDto(
    @field:NotNull val id: Long,
    val username: String?,
    val githubId: String?,
    val githubLink: String?,
    val widgetLink: String?
) {
    constructor(member: Member) : this(
        member.getId(),
        member.username,
        member.githubId,
        member.githubLink,
        member.widgetLink
    )
}
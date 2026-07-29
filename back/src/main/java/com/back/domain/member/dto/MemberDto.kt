package com.back.domain.member.dto

import com.back.domain.member.entity.Member
import jakarta.validation.constraints.NotNull

data class MemberDto(
    @field:NotNull val id: Long,
    val githubId: String?,
    val githubLink: String?
) {
    constructor(member: Member) : this(
        member.getId(),
        member.githubId,
        member.githubLink
    )
}
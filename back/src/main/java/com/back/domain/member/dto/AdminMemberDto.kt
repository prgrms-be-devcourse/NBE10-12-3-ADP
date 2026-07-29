package com.back.domain.member.dto

import com.back.domain.member.entity.Member
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

class AdminMemberDto(
    id: Long,
    githubId: String?,
    githubLink: String?,
    val username: String?,
    val nickname: String?,
    @field:NotNull val isAdmin: Boolean,
    @field:NotNull val isDeleted: Boolean,
    @field:NotNull val createdDate: LocalDateTime
) : MemberDto(id, githubId, githubLink) {
    constructor(member: Member) : this(
        member.getId(),
        member.githubId,
        member.githubLink,
        member.username,
        member.nickname,
        member.isAdmin,
        member.isDeleted,
        member.getCreatedDate()
    )
}
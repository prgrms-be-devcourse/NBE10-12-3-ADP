package com.back.domain.member.dto

import com.back.domain.member.entity.Member
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class AdminMemberDto(
    @field:NotNull val id: Long,
    val username: String?,
    val nickname: String?,
    val githubId: String?,
    @field:NotNull val isAdmin: Boolean,
    @field:NotNull val isDeleted: Boolean,
    @field:NotNull val createdDate: LocalDateTime
) {
    constructor(member: Member) : this(
        member.getId(),
        member.username,
        member.nickname,
        member.githubId,
        member.isAdmin,
        member.isDeleted,
        member.getCreatedDate()
    )
}
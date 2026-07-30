package com.back.domain.member.dto

import com.back.domain.member.entity.Member

class MemberWithUsernameAndWidgetLinkDto(
    id: Long,
    githubId: String?,
    githubLink: String?,
    val username: String?,
    val widgetLink: String?
) : MemberDto(id, githubId, githubLink) {
    constructor(member: Member) : this(
        member.id,
        member.githubId,
        member.githubLink,
        member.username,
        member.widgetLink
    )
}
package com.back.domain.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class MemberLoginRequestDto(
    @NotBlank
    @Size(min = 2, max = 30)
    val username: String,
    @NotBlank
    @Size(min = 2, max = 30)
    val password: String
)
package com.back.domain.member.controller

import com.back.domain.member.dto.AdminMemberDto
import com.back.domain.member.dto.MemberDto
import com.back.domain.member.dto.MemberWithUsernameAndWidgetLinkDto
import com.back.domain.member.service.MemberService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Transactional(readOnly = true)
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
class ApiV1MemberController(
    private val memberService: MemberService,
    private val rq: Rq
) {
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    fun me(): MemberWithUsernameAndWidgetLinkDto {
        val actor = memberService.findById(rq.actor.getId())
        return MemberWithUsernameAndWidgetLinkDto(actor)
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 단건 조회")
    fun getMember(
        @PathVariable @Valid id: Long
    ): MemberDto {
        val member = memberService.findById(id)

        return MemberDto(member)
    }

    data class MemberLoginReqBody(
        @NotBlank
        @Size(min = 2, max = 30)
        val username: String,
        @NotBlank
        @Size(min = 2, max = 30)
        val password: String
    )

    data class MemberLoginResBody(
        val accessToken: String,
        val refreshToken: String?
    )

    @DeleteMapping
    @Transactional
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(): RsData<Void> {
        memberService.delete(rq.actor.getId())

        rq.deleteCookie("refreshToken")
        rq.deleteCookie("accessToken")

        return RsData("200-1", "회원 탈퇴를 성공했습니다.")
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(
        @RequestBody @Valid reqBody: MemberLoginReqBody
    ): RsData<MemberLoginResBody> {
        val member = memberService.findByUsername(reqBody.username)

        memberService.checkPassword(member, reqBody.password)

        val accessToken = memberService.genAccessToken(member)

        rq.setCookie("refreshToken", member.refreshToken)
        rq.setCookie("accessToken", accessToken)

        return RsData(
            "200-1",
            "로그인을 성공했습니다.",
            MemberLoginResBody(
                accessToken,
                member.refreshToken
            )
        )
    }

    data class MemberJoinReqBody(
        @NotBlank
        @Size(min = 2, max = 30)
        val username: String,
        @NotBlank
        @Size(min = 2, max = 30)
        val password: String,
        @NotBlank
        @Size(max = 39)
        @Pattern(
            regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
            message = "githubId는 영문 소문자, 숫자, 하이픈(-)만 사용할 수 있으며 하이픈은 처음/끝/연속으로 올 수 없습니다."
        )
        val githubId: String
    )

    @PostMapping
    @Operation(summary = "회원 가입")
    @Transactional
    fun join(
        @RequestBody @Valid reqBody: MemberJoinReqBody
    ): RsData<MemberLoginResBody> {
        val member = memberService.join(
            reqBody.username,
            reqBody.password,
            reqBody.githubId,
            null
        )

        memberService.checkPassword(member, reqBody.password)

        val accessToken = memberService.genAccessToken(member)

        rq.setCookie("refreshToken", member.refreshToken)
        rq.setCookie("accessToken", accessToken)

        return RsData(
            "200-1",
            "회원가입을 성공했습니다.",
            MemberLoginResBody(
                accessToken,
                member.refreshToken
            )
        )
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    fun logout(): RsData<Void> {
        rq.deleteCookie("refreshToken")
        rq.deleteCookie("accessToken")

        return RsData("200-1", "로그아웃을 성공했습니다.")
    }

    @GetMapping("/admin")
    @Operation(summary = "회원 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun getMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<AdminMemberDto> {
        return memberService.getMembers(page, size).map { AdminMemberDto(it) }
    }

    @DeleteMapping("/admin/{id}")
    @Transactional
    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteMember(
        @PathVariable id: Long
    ): RsData<Void> {
        memberService.delete(id)

        return RsData("200-1", "회원 강제 탈퇴를 성공했습니다.")
    }
}
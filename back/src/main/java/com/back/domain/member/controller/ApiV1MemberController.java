package com.back.domain.member.controller;

import com.back.domain.member.dto.AdminMemberDto;
import com.back.domain.member.dto.MemberDto;
import com.back.domain.member.dto.MemberWithUsernameAndWidgetLinkDto;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    public MemberWithUsernameAndWidgetLinkDto me() {
        Member actor = memberService.findById(rq.getActor().getId());
        return new MemberWithUsernameAndWidgetLinkDto(actor);
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 정보 조회")
    public MemberDto getUser(
            @PathVariable @Valid long id
    ) {
        Member member = memberService.findById(id);

        return new MemberDto(member);
    }

    public record MemberLoginReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String username,
            @NotBlank
            @Size(min = 2, max = 30)
            String password
    ) {
    }

    public record MemberLoginResBody(
            String accessToken,
            String refreshToken
    ) {
    }

    @DeleteMapping
    @Transactional
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<Void> delete() {

        memberService.delete(rq.getActor().getId());

        rq.deleteCookie("refreshToken");
        rq.deleteCookie("accessToken");

        return new RsData<>("200-1", "회원 탈퇴 성공");
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<MemberLoginResBody> login(
            @RequestBody @Valid MemberLoginReqBody reqBody
    ) {

        Member member = memberService.findByUsername(reqBody.username());

        memberService.checkPassword(member, reqBody.password());

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("refreshToken", member.getRefreshToken());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "로그인 성공",
                new MemberLoginResBody(
                        accessToken,
                        member.getRefreshToken()
                )
        );

    }

    public record MemberJoinReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String username,
            @NotBlank
            @Size(min = 2, max = 30)
            String password,
            @NotBlank
            @Size(max = 39)
            @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "githubId는 영문 소문자, 숫자, 하이픈(-)만 사용할 수 있으며 하이픈은 처음/끝/연속으로 올 수 없습니다.")
            String githubId
    ) {
    }

    @PostMapping
    @Operation(summary = "회원 가입")
    @Transactional
    public RsData<MemberLoginResBody> join(
            @RequestBody @Valid MemberJoinReqBody reqBody
    ) {

        Member member = memberService.join(
                reqBody.username(),
                reqBody.password(),
                reqBody.githubId(),
                null
        );

        memberService.checkPassword(member, reqBody.password());

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("refreshToken", member.getRefreshToken());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "회원가입 성공",
                new MemberLoginResBody(
                        accessToken,
                        member.getRefreshToken()
                )
        );

    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<Void> logout() {
        rq.deleteCookie("refreshToken");
        rq.deleteCookie("accessToken");

        return new RsData<>("200-1", "로그아웃 성공");
    }

    @GetMapping("/admin")
    @Operation(summary = "회원 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public Page<AdminMemberDto> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return memberService.getMembers(page, size).map(AdminMemberDto::new);
    }

    @DeleteMapping("/admin/{id}")
    @Transactional
    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<Void> deleteMember(
            @PathVariable long id
    ) {
        memberService.delete(id);

        return new RsData<>("200-1", "회원 강제 탈퇴 완료");
    }

}

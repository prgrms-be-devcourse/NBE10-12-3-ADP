package com.back.domain.member.controller;

import com.back.domain.member.controller.ApiV1MemberController;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("내 정보 조회")
    @WithUserDetails("user1")
    void t1() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me"))
                .andDo(print());

        Member member = memberService.findByUsername("user1");

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.username").value(member.getUsername()))
                .andExpect(jsonPath("$.githubId").value(member.getGithubId()))
                .andExpect(jsonPath("$.githubLink").value(member.getGithubLink()))
                .andExpect(jsonPath("$.widgetLink").value(member.getWidgetLink()));

    }

    @Test
    @DisplayName("회원 정보 조회")
    void t2() throws Exception {

        Long id = 1L;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/%d".formatted(id)))
                .andDo(print());

        Member member = memberService.findById(id);

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.githubId").value(member.getGithubId()))
                .andExpect(jsonPath("$.githubLink").value(member.getGithubLink()));

    }

    @Test
    @DisplayName("회원 탈퇴")
    @WithUserDetails("user1")
    void t3() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"));

        resultActions.andExpect(
                result -> {
                    Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");

                    assertThat(refreshTokenCookie.getValue()).isEmpty();
                    assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
                    assertThat(refreshTokenCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");

                    assertThat(accessTokenCookie.getValue()).isEmpty();
                    assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );
    }

    @Test
    @DisplayName("로그인")
    void t4() throws Exception {

        Long id = 1L;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234"
                                        }
                                        """))
                .andDo(print());

        Member member = memberService.findByUsername("user1");

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").value(member.getRefreshToken()));

        resultActions.andExpect(
                result -> {
                    Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");

                    assertThat(refreshTokenCookie.getValue()).isEqualTo(member.getRefreshToken());
                    assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
                    assertThat(refreshTokenCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");

                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );

    }


    @Test
    @DisplayName("회원가입")
    void t5() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "user7",
                                            "password": "1234",
                                            "githubId": "easy-h"
                                        }
                                        """))
                .andDo(print());

        Member member = memberService.findByUsername("user7");

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").value(member.getRefreshToken()));

        resultActions.andExpect(
                result -> {
                    Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");

                    assertThat(refreshTokenCookie.getValue()).isEqualTo(member.getRefreshToken());
                    assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
                    assertThat(refreshTokenCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");

                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );

    }

    @Test
    @DisplayName("회원 다건 조회 - 관리자")
    @WithUserDetails("admin")
    void t7() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/admin")
                                .param("page", "0")
                                .param("size", "10"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").exists())
                .andExpect(jsonPath("$.content[0].isAdmin").exists())
                .andExpect(jsonPath("$.content[0].isDeleted").exists());

    }

    @Test
    @DisplayName("회원 다건 조회 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    void t8() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/admin"))
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));

    }

    @Test
    @DisplayName("회원 강제 탈퇴 - 관리자")
    @WithUserDetails("admin")
    void t9() throws Exception {

        Member member = memberService.findByUsername("user2");

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/admin/%d".formatted(member.getId())))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("deleteMember"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("회원 강제 탈퇴 완료"));

        assertThat(memberRepository.findById(member.getId()).orElseThrow().isDeleted()).isTrue();

    }

    @Test
    @DisplayName("회원 강제 탈퇴 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    void t10() throws Exception {

        Member member = memberService.findByUsername("user2");

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/admin/%d".formatted(member.getId())))
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));

    }

    @Test
    @DisplayName("로그아웃")
    @WithUserDetails("user1")
    void t6() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/logout"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));

        resultActions.andExpect(
                result -> {
                    Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");

                    assertThat(refreshTokenCookie.getValue()).isEmpty();
                    assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
                    assertThat(refreshTokenCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");

                    assertThat(accessTokenCookie.getValue()).isEmpty();
                    assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );

    }
}
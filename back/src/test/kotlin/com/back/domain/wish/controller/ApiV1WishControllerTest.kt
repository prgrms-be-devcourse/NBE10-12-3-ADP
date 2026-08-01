package com.back.domain.wish.controller

import com.back.domain.book.service.BookService
import com.back.domain.member.service.MemberService
import com.back.domain.wish.entity.Wish
import com.back.domain.wish.service.WishService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ApiV1WishControllerTest {
    @Autowired
    private lateinit var context: WebApplicationContext
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var wishService: WishService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var bookService: BookService

    @BeforeEach
    fun setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("내 찜 다건 조회")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t1() {
        val actor = memberService.getByUsername("user1")

        val wishes: List<Wish> = wishService.getWishesByMember(actor)
        val wishesSize = wishes.size

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/wishes/mine")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1WishController::class.java))
            .andExpect(handler().methodName("getWishes"))
            .andExpect(status().isOk())

        for (i in 0..<wishesSize) {
            val book = wishes[i].book
            val tags: List<String> = bookService.getBookTags(book)
            resultActions
                .andExpect(jsonPath("$.[$i].id").value(book.id))
                .andExpect(jsonPath("$.[$i].title").value(book.title))
                .andExpect(jsonPath("$.[$i].imgUrl").value(book.imgUrl))
                .andExpect(jsonPath("$.[$i].tags").isArray())
                .andExpect(
                    jsonPath("$.[$i].averageRating").value(book.averageRating)
                )

            for (j in tags.indices) {
                resultActions
                    .andExpect(jsonPath("$[$i].tags[$j]").value(tags[j]))
            }
        }
    }

    @Test
    @DisplayName("찜 생성")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t2() {
        val bookId = 2L

        val resultActions = mvc
            .perform(
                post("/api/v1/wishes/book/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1WishController::class.java))
            .andExpect(handler().methodName("addWish"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.message").value("찜 생성을 성공했습니다."))
    }

    @Test
    @DisplayName("찜 삭제(구형)")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val bookId = 1L

        val resultActions = mvc
            .perform(
                delete("/api/v1/wishes/book/$bookId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1WishController::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("찜 삭제를 성공했습니다."))
    }

    @Test
    @DisplayName("찜 삭제")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t4() {
        val wishId = 1L

        val resultActions = mvc
            .perform(
                delete("/api/v1/wishes/$wishId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1WishController::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("찜 삭제를 성공했습니다."))
    }
}
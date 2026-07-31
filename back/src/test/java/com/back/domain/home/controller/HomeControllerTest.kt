package com.back.domain.home.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HomeControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("헬스체크")
    fun t1() {
        val resultActions = mvc
            .perform(
                get("/health")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(HomeController::class.java))
            .andExpect(status().isOk)
    }
}

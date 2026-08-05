package com.back.domain.book.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookOperationalRepositoryExplainTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @DisplayName("BookOperational 정렬용 인덱스가 생성된다")
    fun t1() {
        val indexNames = jdbcTemplate.queryForList(
            """
                SELECT INDEX_NAME
                FROM INFORMATION_SCHEMA.INDEXES
                WHERE TABLE_NAME = 'BOOK_OPERATIONAL'
            """.trimIndent(),
            String::class.java
        )

        assertThat(indexNames).contains(
            "IDX_BOOK_AVERAGE_RATING_ID",
            "IDX_BOOK_REVIEW_COUNT_ID",
            "IDX_BOOK_VIEW_COUNT_ID"
        )
    }

    @Test
    @DisplayName("BookOperational 조회수 정렬 쿼리는 인덱스 사용 계획을 가진다")
    fun t2() {
        val plan = jdbcTemplate.queryForObject(
            """
                EXPLAIN
                SELECT ISBN
                FROM BOOK_OPERATIONAL
                ORDER BY VIEW_COUNT DESC, ID DESC
                LIMIT 10
            """.trimIndent(),
            String::class.java
        ) ?: ""

        assertThat(plan).contains("IDX_BOOK_VIEW_COUNT_ID")
    }
}

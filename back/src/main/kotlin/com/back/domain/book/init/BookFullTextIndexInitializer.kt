package com.back.domain.book.init

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

/**
 * Hibernate가 book 테이블을 만든 뒤, MySQL Ngram FULLTEXT 인덱스를 보장한다.
 *
 * JPA의 schema update는 `WITH PARSER ngram`을 표현하지 못하므로 이 작업은
 * Flyway 도입 전까지만 유지한다.
 */
@Component
class BookFullTextIndexInitializer(
    private val dataSource: DataSource
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        dataSource.connection.use { connection ->
            if (!connection.metaData.databaseProductName.equals("MySQL", ignoreCase = true)) {
                log.info("Skipping book FULLTEXT index initialization: database is not MySQL")
                return
            }

            if (hasFullTextIndex(connection)) {
                log.info("Book Ngram FULLTEXT index already exists")
                return
            }

            connection.createStatement().use { statement ->
                // SESSION 범위이므로 다른 연결 및 MySQL 전역 설정에는 영향을 주지 않는다.
                statement.execute("SET SESSION innodb_ft_enable_stopword = OFF")
                statement.execute(
                    "CREATE FULLTEXT INDEX ft_book_search " +
                        "ON book (title, authors, publisher) WITH PARSER ngram"
                )
            }

            log.info("Created Book Ngram FULLTEXT index")
        }
    }

    private fun hasFullTextIndex(connection: Connection): Boolean =
        connection.prepareStatement(
            """
                SELECT 1
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'book'
                  AND index_name = 'ft_book_search'
                  AND index_type = 'FULLTEXT'
                LIMIT 1
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { it.next() }
        }

    companion object {
        private val log = LoggerFactory.getLogger(BookFullTextIndexInitializer::class.java)
    }
}

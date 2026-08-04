package com.back.domain.book.service

import com.back.domain.book.dto.BookDetailDto
import com.back.domain.book.dto.BookDto
import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookOperational
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import com.back.global.rq.Rq
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.iterator

@Service
@Transactional(readOnly = true)
class BookService(
    private val rq: Rq,
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val bookViewCountRedisRepository: BookViewCountRedisRepository,
    private val bookThumbnailService: BookThumbnailService,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
) {

    fun getBookById(bookId: Long): Book {
        return bookRepository.findByIdOrNull(bookId)
            ?: throw NoSuchElementException("존재하지 않는 도서입니다.")
    }

    private fun getBookByIsbn(isbn: String)
        = bookRepository.findByIsbn(isbn)

    @Transactional
    fun updateBookViewCountInDb(book: Book, viewCount: Int) {

        val bookOperational = getBookOperational(book)
            ?: bookOperationalRepository.save(BookOperational(book.isbn))

        bookOperational.viewCount = viewCount

    }

    private fun getBookOperational(book: Book)
        = bookOperationalRepository.findByIsbn(book.isbn)

    private fun getDBBookViewCount(book: Book)
        = getBookOperational(book)?.viewCount ?: 0

    private fun getBookDto(book: Book) : BookDto {
        return BookDto(book, getBookOperational(book)?.averageRating ?: 0.0)
    }

    private fun getBookDtosFromOperations(operations: List<BookOperational>): List<BookDto> {
        val booksByIsbn = bookRepository.findByIsbnIn(operations.map { it.isbn })
            .associateBy { it.isbn }

        return operations.mapNotNull { operation ->
            booksByIsbn[operation.isbn]?.let { BookDto(it, operation.averageRating) }
        }
    }

    private fun getBookDtosByBookIds(bookIds: List<Long>): List<BookDto> {
        val books = bookRepository.findAllById(bookIds)
        val booksById = books.associateBy { it.id }
        val ratingsByIsbn = bookOperationalRepository.findByIsbnIn(books.map { it.isbn })
            .associate { it.isbn to it.averageRating }

        return bookIds.mapNotNull { bookId ->
            booksById[bookId]?.let { BookDto(it, ratingsByIsbn[it.isbn] ?: 0.0) }
        }
    }

    fun getBookViewCount(book: Book): Int {

        return bookViewCountRedisRepository
            .findBookViewCountById(book.id)
            ?: getDBBookViewCount(book)
    }

    fun getBooksOrderByTopViewedInLastHour(page: Int, size: Int): List<BookDto> {
        val books = bookViewCountRedisRepository.findBookIdOrderByTopViewedInLastHout(page, size)
            ?: return bookOperationalRepository
                .findAllByOrderByViewCountDesc(
                    PageRequest.of(page, size)
                ).toList()
                .let { getBookDtosFromOperations(it) }

        return getBookDtosByBookIds(books)
    }

    fun getBooksOrderByRank(type: String, page: Int, size: Int): List<BookDto> {

        if (type == "views")
            return getBooksOrderByTopViewedInLastHour(page, size)

        val pageable = PageRequest.of(page, size)

        if (type == "rating")
            return bookOperationalRepository
                .findAllByOrderByAverageRatingDesc(pageable).toList()
                .let { getBookDtosFromOperations(it) }

        return bookOperationalRepository
            .findAllByOrderByReviewCountDesc(pageable).toList()
            .let { getBookDtosFromOperations(it) }
    }

    @Transactional
    fun incrementViewCount(book: Book) {
        if (rq.getCookieValue("viewed-%d".format(book.id), "") == "true") {
            return
        }

        if (!bookViewCountRedisRepository
            .tryIncreaseViewAtRedis(book.id)
            { getDBBookViewCount(book) }) {
            updateBookViewCountInDb(book, getDBBookViewCount(book) + 1)
        }

        rq.setCookie("viewed-%d".format(book.id), "true", 60)

    }

    @Transactional
    fun updateBooksViewCountInDb() {

        val viewMap = bookViewCountRedisRepository.findAllBookViews()

        for (tuple in viewMap) {
            val book = bookRepository.findByIdOrNull(tuple.key) ?: continue
            updateBookViewCountInDb(book, tuple.value)
        }
    }

    fun getBook(id: Long): Book {
        val book = getBookById(id)
        incrementViewCount(book)

        if (book.imgUrl.isNullOrBlank()) {
            bookThumbnailService.fillMissingImgUrl(book.id, book.isbn)?.let { book.updateImgUrl(it) }
        }

        return book
    }

    fun getBookDetail(id: Long, actor: Member?): BookDetailDto {

        val book = getBook(id)

        return BookDetailDto(
            book,
            getBookOperational(book)?.reviewCount ?: 0,
            getIsWished(book, actor),
            getRatingMap(book),
            getBookTags(book)
        )

    }

    @Transactional
    fun updateBook(
        id: Long,
        title: String,
        description: String?,
        authors: String?,
        publisher: String?,
        imgUrl: String?
    ): BookDto {
        val book = getBookById(id)

        book.update(title, description, authors, publisher, imgUrl)

        return getBookDto(book)
    }

    @Transactional
    fun deleteBook(id: Long) {
        val book = getBookById(id)

        reviewRepository.deleteAll(reviewRepository.findByBook(book))
        wishRepository.deleteAllByBook(book)

        bookRepository.delete(book)
    }

    fun getBooks(page: Int, size: Int): Page<BookDto> {
        return bookRepository
            .findAll(
                PageRequest.of(
                    page,
                    size,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
                )
            )
            .map { getBookDto(it) }
    }


    fun getBookTags(book: Book): List<String> {
        val reviews: List<Review> = reviewRepository.findByBook(book)

        return reviews
            .flatMap { r: Review -> r.tags }
            .distinct()
            .toList()
    }

    fun getTagsByBookId(bookId: Long): List<String> {
        val reviews: List<Review> = reviewRepository.findByBookId(bookId)

        return reviews
            .flatMap { r: Review -> r.tags }
            .distinct()
            .toList()
    }

    fun getBooksBySearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): List<BookDto> {
        val pageable = PageRequest.of(page, size)

        try {
            return bookRepository.searchByKeyword(searchTerm, pageable)
                .toList().map { getBookDto(it) }

        } catch (_: java.lang.Exception) {
            // handler();
        }

        return bookRepository.findByTitleContaining(searchTerm, pageable)
            .toList().map { getBookDto(it) }
    }

    fun getIsWished(book: Book, actor: Member?): Boolean {

        if (actor == null) return false

        return wishRepository.findByMemberAndBook(actor, book) != null
    }

    fun getRatingMap(book: Book): MutableMap<String, Any> {
        val ratingMap: MutableMap<String, Any> = mutableMapOf()

        ratingMap["average"] =
            getBookOperational(book)?.averageRating ?: 0

        for (i in 1..10) {
            val rating = i * 0.5f
            ratingMap["%.1f".format(rating)] =
                reviewRepository.countByBookAndRating(book, rating)
        }

        return ratingMap
    }
}

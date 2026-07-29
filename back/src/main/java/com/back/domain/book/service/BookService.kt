package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import com.back.global.rq.Rq
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.iterator

@Service
@Transactional(readOnly = true)
class BookService(
    private val rq: Rq,
    private val bookRepository: BookRepository,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
    private val bookViewCountRedisRepository: BookViewCountRedisRepository

) {

    fun getBookById(bookId: Long) : Book {
        val book = bookRepository.findById(bookId)
        return if (book.isPresent) book.get()
            else throw NoSuchElementException("존재하지 않는 도서입니다.")

    }

    @Transactional
    fun updateBooksViewCountInDb(book: Book, viewCount: Int) {
        book.viewCount = viewCount
    }

    fun getBookViewCount(book: Book): Int {
        val viewCount = bookViewCountRedisRepository
            .findBookViewCountById(book.id)

        if (viewCount != null)
            return viewCount

        return book.viewCount
    }

    fun getBooksOrderByTopViewedInLastHour(page: Int, size: Int): List<Book> {
        val books = bookViewCountRedisRepository.findBookIdOrderByTopViewedInLastHout(page, size) ?: return listOf()

        return books.map { bookId: Long -> getBookById(bookId) }
    }

    @Transactional
    fun incrementViewCount(book: Book) {
        if (rq.getCookieValue("viewed:%d".format(book.id), "") == "true") {
            return
        }

        if (bookViewCountRedisRepository
                .increase(book.id) { book.viewCount}) return

        updateBooksViewCountInDb(book, book.viewCount + 1)

        rq.setCookie("viewed:%d".format(book.id), "true", 60)

    }

    @Transactional
    fun updateBooksViewCountInDb() {

        val viewMap = bookViewCountRedisRepository.findAllBookViews()

        for (tuple in viewMap) {
            val book = bookRepository.findById(tuple.key)
            if (book.isEmpty) continue
            updateBooksViewCountInDb(book.get(), tuple.value)
        }
    }

    fun getBook(id: Long): Book {
        val book = getBookById(id)
        incrementViewCount(book)
        return book
    }

    @Transactional
    fun updateBook(
        id: Long,
        title: String,
        description: String?,
        authors: String?,
        publisher: String?,
        imgUrl: String?
    ): Book {
        val book = getBookById(id)

        book.modify(title, description, authors, publisher, imgUrl)

        return book
    }

    @Transactional
    fun deleteBook(id: Long) {
        val book = getBookById(id)

        reviewRepository.deleteAll(reviewRepository.findByBook(book))
        wishRepository.deleteAllByBook(book)

        bookRepository.delete(book)
    }

    fun getBooks(page: Int, size: Int): Page<Book> {
        return bookRepository
            .findAll(
                PageRequest.of(
                    page,
                    size,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
                )
            )
    }


    fun getBookTags(book: Book): List<String> {
        val reviews: List<Review> = reviewRepository.findByBook(book)

        return reviews
            .flatMap { r: Review -> r.tags }
            .distinct()
            .toList()
    }

    fun getBooksBySearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): Page<Book> {
        val pageable = PageRequest.of(page, size)

        try {
            return bookRepository.searchByKeyword(searchTerm, pageable)
        } catch (_: java.lang.Exception) {
            // handler();
        }

        return bookRepository.findByTitleContaining(searchTerm, pageable)
    }

    fun getIsWished(book: Book, actor: Member?) : Boolean {

        if (actor == null) return false

        return wishRepository.findByMemberAndBook(actor, book).isPresent
    }

    fun getRatingMap(book: Book): MutableMap<String, Any> {
        val ratingMap: MutableMap<String, Any> = mutableMapOf()

        ratingMap["average"] = book.averageRating

        for (i in 1..10) {
            val rating = i * 0.5f
            ratingMap["%.1f".format(rating)] = reviewRepository.countByBookAndRating(book, rating)
        }

        return ratingMap
    }
}

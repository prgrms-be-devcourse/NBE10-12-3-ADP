package com.back.domain.book.service;

import com.back.domain.book.dto.BookDetailDto;
import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.wish.repository.WishRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final WishRepository wishRepository;

    @Transactional
    public Book editBook(Long id, String title, String description, String authors, String publisher, String imgUrl) {
        Book book = getPureBook(id);

        book.modify(title, description, authors, publisher, imgUrl);

        return book;
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getPureBook(id);

        reviewRepository.deleteAll(reviewRepository.findByBook(book));
        wishRepository.deleteAllByBook(book);

        bookRepository.delete(book);
    }

    public Page<BookDto> getBooks(int page, int size) {
        return bookRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(BookDto::new);
    }

    public Book getPureBook(Long id) throws NoSuchElementException {
        return bookRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 도서입니다."));
    }

    public List<String> getBookTags(Book book) {

        List<Review> reviews = reviewRepository.findByBook(book);

        return reviews.stream()
                .flatMap(r -> r.getTags().stream())
                .distinct()
                .toList();

    }

    public BookDetailDto getBook(Long id, Member actor) {
        Book book = getPureBook(id);

        Map<String, Object> ratingMap = buildRatingMap(book);


        boolean isWished = false;
        if (actor != null) {
            isWished = wishRepository.findByMemberAndBook(actor, book).isPresent();
        }

        return new BookDetailDto(book, isWished, ratingMap, getBookTags(book));
    }

    public List<BookDto> search(String searchTerm, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        try {
            return bookRepository.searchByKeyword(searchTerm, pageable)
                    .stream().map(BookDto::new).toList();
        } catch (Exception e) {
            // handler();
        }

        return bookRepository.findByTitleContaining(searchTerm).stream()
                .map(BookDto::new)
                .toList();
    }

    private Map<String, Object> buildRatingMap(Book book) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("average", book.getAverageRating());
        for (int i = 1; i <= 10; i++) {
            float rating = i * 0.5f;
            ratingMap.put("%.1f".formatted(rating), reviewRepository.countByBookAndRating(book, rating));
        }
        return ratingMap;
    }
}

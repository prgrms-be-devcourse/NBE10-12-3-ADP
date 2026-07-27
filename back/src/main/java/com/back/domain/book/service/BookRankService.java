package com.back.domain.book.service;

import com.back.domain.book.dto.BookDetailDto;
import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookRankService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public List<BookDto> getBooksByRating(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAllByOrderByAverageRatingDesc(pageable)
                .stream()
                .map(BookDto::new)
                .toList();
    }

    public List<BookDto> getBooksByReviewCnt(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return bookRepository.findAllByOrderByReviewCountDesc(pageable)
                .stream()
                .map(BookDto::new)
                .toList();
    }

}

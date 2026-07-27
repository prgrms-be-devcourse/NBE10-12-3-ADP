package com.back.domain.book.service;

import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookViewsService {

    private final Rq rq;
    private final RedisTemplate<String, String> redisTemplate;
    private final BookRepository bookRepository;
    private final BookService bookService;


    private String getMinuteKey() {
        return "viewCount:minute:%s".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }

    private int getDBViewCount(Long bookId) {

        Optional<Book> book = bookRepository.findById(bookId);

        return book.map(Book::getViewCount).orElse(0);
    }

    public int getViewCount(Long bookId) {

        try {
            Double score = redisTemplate.opsForZSet()
                    .score("viewCount", bookId.toString());

            if (score != null) {
                return score.intValue();
            }
        } catch (Exception e) {
            // log.warn(...)

        }

        return getDBViewCount(bookId);

    }

    public List<BookDto> topViewedInLastHour(int page, int size) {

        try {
            int start = page * size;
            int end = start + size - 1;

            LocalDateTime now = LocalDateTime.now();

            List<String> keys = new ArrayList<>();

            for (int i = 0; i < 60; i++) {
                String key = "viewCount:minute:%s".formatted(
                        now.minusMinutes(i)
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                );

                keys.add(key);
            }

            String tempKey = "viewCount:lastHour:temp:" + UUID.randomUUID();

            redisTemplate.delete(tempKey);

            redisTemplate.opsForZSet().unionAndStore(
                    keys.getFirst(),
                    keys.subList(1, keys.size()),
                    tempKey
            );

            redisTemplate.expire(tempKey, Duration.ofSeconds(10));

            Set<String> bookIds = redisTemplate.opsForZSet()
                    .reverseRange(tempKey, start, end);

            if (bookIds == null || bookIds.isEmpty()) {
                return List.of();
            }

            return bookIds.stream()
                    .map(bookId -> new BookDto(bookService.getPureBook(Long.parseLong(bookId))))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public void incrementViewCount(Long bookId) {

        try {
            if (rq.getCookieValue("viewed:%d".formatted(bookId), "").equals("true")) {
                return;
            }

            if (redisTemplate.opsForZSet().score("viewCount", bookId.toString()) == null) {
                redisTemplate.opsForZSet().add("viewCount", bookId.toString(), getDBViewCount(bookId));
            }

            redisTemplate.opsForZSet().incrementScore("viewCount", bookId.toString(), 1);

            String minuteKey = getMinuteKey();
            redisTemplate.opsForZSet().incrementScore(
                    minuteKey, bookId.toString(), 1);
            redisTemplate.expire(minuteKey, Duration.ofMinutes(70));

            rq.setCookie("viewed:%d".formatted(bookId), "true", 60);
        } catch (Exception e) {
            updateViewCountInDb(bookId, getDBViewCount(bookId));

        }
    }

    @Transactional
    public void updateViewCountInDb() {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores("viewCount", 0, -1);

            if (tuples == null || tuples.isEmpty()) return;

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple.getValue() == null || tuple.getScore() == null) continue;

                try {
                    Long bookId = Long.parseLong(tuple.getValue());
                    int viewCount = tuple.getScore().intValue();

                    updateViewCountInDb(bookId, viewCount);
                } catch (Exception e) {
                    // log.warn(...)
                }
            }
        } catch (Exception e) {
            // log.warn(...)
        }
    }

    @Transactional
    public void updateViewCountInDb(Long bookId, int viewCount) {
        Optional<Book> bookOp = bookRepository.findById(bookId);
        if (bookOp.isEmpty()) return;

        bookOp.get().setViewCount(viewCount);
    }
}

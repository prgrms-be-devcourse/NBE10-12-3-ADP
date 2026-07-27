package com.back.domain.book.scheduler;

import com.back.domain.book.service.BookService;
import com.back.domain.book.service.BookViewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookViewsScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final BookViewsService bookViewsService;

    @Scheduled(cron = "0 */5 * * * *")
    public void syncViewCountsToDb() {
        bookViewsService.updateViewCountInDb();
    }

}

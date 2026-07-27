package com.back.domain.book.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class BookFetchProgress extends BaseEntity {

    private int currentPage = 1;
    private int currentApiKeyIndex = 0;

    private int dailyCallCount = 0;
    private LocalDate lastCallDate = LocalDate.now();

    public void nextPage() {
        this.currentPage++;
    }

    public void nextApiKeyIndex() {
        this.currentApiKeyIndex++;
        this.dailyCallCount = 0;
    }

    public void incrementCallCount() {
        LocalDate today = LocalDate.now();
        if (!today.isEqual(this.lastCallDate)) {
            // 자정이 지나 다음 날이 되면 모든 상태 초기화
            this.lastCallDate = today;
            this.dailyCallCount = 0;
            this.currentApiKeyIndex = 0;
        }
        this.dailyCallCount++;
    }
}

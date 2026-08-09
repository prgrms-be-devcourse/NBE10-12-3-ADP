package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity

@Entity
class BookLastFetchedPage : BaseEntity() {
    final var number = 0
        private set

    fun increaseNumber() {
        number++
    }
}

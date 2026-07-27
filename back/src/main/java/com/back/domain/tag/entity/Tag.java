package com.back.domain.tag.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Tag extends BaseEntity {

    @Column(unique = true)
    private String name;

    public Tag(String name) {
        super();
        this.name = name;
    }
}

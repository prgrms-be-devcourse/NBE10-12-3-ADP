package com.back.domain.member.entity;

import com.back.domain.book.entity.Book;
import com.back.domain.wish.entity.Wish;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String githubId;
    @Column(unique = true)
    private String githubLink;
    @Column(unique = true)
    private String widgetLink;
    private String password;
    private String nickname;
    private String imgUrl;
    @Column(unique = true)
    private String refreshToken;
    @Setter
    private LocalDateTime deletedDate;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wish> wishes = new ArrayList<>();

    public Member(long id, String username, String name, Role role) {
        setId(id);
        this.username = username;
        setName(name);
        this.deletedDate = null;
        this.imgUrl = null;
        this.role = role;
    }

    public Member(String username, String password, String githubId, String nickname, String imgUrl) {
        this.username = username;
        this.password = password;
        this.githubId = githubId;
        this.nickname = nickname;
        this.refreshToken = UUID.randomUUID().toString();
        this.deletedDate = null;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return nickname;
    }

    public void setName(String name) {
        this.nickname = name;
    }

    public void modifyRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void grantAdmin() {
        this.role = Role.ADMIN;
    }

    public boolean isDeleted() {
        return deletedDate != null;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesAsStringList()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private List<String> getAuthoritiesAsStringList() {
        List<String> authorities = new ArrayList<>();

        authorities.add("ROLE_" + role.name());

        return authorities;
    }

    public void addWish(Book book) {
        wishes.add(new Wish(this, book));
    }

    public void deleteWish(Book book) {
        wishes = wishes
                .stream()
                .filter(w -> !w.getBook().equals(book))
                .toList();
    }

    public void modify(String nickname) {
        this.nickname = nickname;
    }
}

package com.back.global.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    private val id: Long,
    username: String,
    private val name: String,
    authorities: Collection<GrantedAuthority>
) : User(username, "", authorities), OAuth2User {

    override fun getAttributes() = mapOf<String, Any>()
    override fun getName() = name
}
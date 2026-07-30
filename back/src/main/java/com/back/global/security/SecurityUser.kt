package com.back.global.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: Long,
    username: String,
    name: String,
    authorities: Collection<GrantedAuthority>
) : User(username, "", authorities), OAuth2User {
    private val _name = name

class SecurityUser(
    val id: Long,
    username: String,
    override val name: String,
    authorities: Collection<GrantedAuthority>
) : User(username, "", authorities), OAuth2User {

    override fun getAttributes() = mapOf<String, Any>()
}
    override fun getName() = _name
}
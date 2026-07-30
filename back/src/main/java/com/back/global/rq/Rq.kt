package com.back.global.rq

import com.back.domain.member.entity.Member
import com.back.domain.member.entity.Role
import com.back.domain.member.service.MemberService
import com.back.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Predicate

@Component
class Rq(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val memberService: MemberService

) {

    @Value($$"${custom.security.cookieSecure}")
    private val isSecure = false

    @Value($$"${custom.security.cookieDomain}")
    private val cookieDomain: String? = null

    val actor: Member?
        get() = SecurityContextHolder
            .getContext()
            .authentication
            ?.principal
            ?.let {
                if (it is SecurityUser) { Member(it.id, it.username, it.name,
                    if (hasAdminAuthority(it.authorities)) Role.ADMIN else Role.USER)}
                else null
            }


    private fun hasAdminAuthority(authorities: MutableCollection<out GrantedAuthority?>): Boolean {
        return authorities.stream()
            .anyMatch { authority: GrantedAuthority? -> "ROLE_ADMIN" == authority!!.authority }
    }

    fun getHeader(name: String?, defaultValue: String?): String? {
        return Optional
            .ofNullable<String?>(req.getHeader(name))
            .filter(Predicate { headerValue: String? -> !headerValue!!.isBlank() })
            .orElse(defaultValue)
    }

    fun setHeader(name: String?, value: String?) {
        val value = value ?: ""

        if (value.isBlank()) {
            req.removeAttribute(name)
        } else {
            resp.setHeader(name, value)
        }
    }

    fun getCookieValue(name: String, defaultValue: String?): String? {
        return Arrays.stream<Cookie?>(
            Optional.ofNullable<Array<Cookie?>?>(req.cookies).orElse(arrayOfNulls(0))
        )
            .filter { cookie: Cookie? -> name == cookie!!.name }
            .map { obj: Cookie? -> obj!!.value }
            .filter { value: String? -> !value.isNullOrBlank() }
            .findFirst()
            .orElse(defaultValue)
    }

    fun setCookie(name: String?, value: String?, maxAge: Int) {
        var value = value
        if (value == null) value = ""

        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.domain = cookieDomain
        cookie.secure = isSecure
        cookie.setAttribute("SameSite", if (isSecure) "None" else "Strict")

        if (value.isBlank()) cookie.maxAge = 0
        else cookie.maxAge = maxAge

        resp.addCookie(cookie)
    }

    fun setCookie(name: String?, value: String?) {
        setCookie(name, value, 60 * 60 * 24 * 365)
    }

    fun deleteCookie(name: String?) {
        setCookie(name, null)
    }

    fun sendRedirect(url: String?) {
        resp.sendRedirect(url)
    }

    val actorFromDb: Member?
        get() {
            val actor = this.actor ?: return null

            return memberService.getById(actor.id)
        }
}
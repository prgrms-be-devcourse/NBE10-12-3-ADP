package com.back.domain.member.service

import com.back.domain.member.entity.Member
import com.back.domain.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.NoSuchElementException
import java.util.Optional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    fun getById(id: Long): Member {
        val member = memberRepository.findById(id)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원입니다.") }

        if (member.isDeleted) {
            throw NoSuchElementException("존재하지 않는 회원입니다.")
        }

        return member
    }

    fun getByUsername(username: String): Member {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("존재하지 않는 회원입니다.") }

        if (member.isDeleted) {
            throw ServiceException("404-1", "존재하지 않는 회원입니다.")
        }
        return member
    }

    @Transactional
    fun join(username: String, password: String, githubId: String, imgUrl: String?): Member =
        join(username, password, githubId, githubId, imgUrl)

    @Transactional
    fun join(username: String, password: String, githubId: String?, nickname: String, imgUrl: String?): Member {
        memberRepository.findByUsername(username).ifPresent {
            throw ServiceException("409-1", "이미 존재하는 아이디입니다.")
        }

        if (githubId != null) {
            memberRepository.findByGithubId(githubId).ifPresent {
                throw ServiceException("409-2", "이미 존재하는 githubId입니다.")
            }
        }

        return memberRepository.save(
            Member(
                username = username,
                password = encodePasswordImplementation(password),
                githubId = githubId,
                nickname = nickname,
                imgUrl = imgUrl
            )
        )
    }

    @Transactional
    fun delete(id: Long) {
        val member = getById(id)
        member.deletedDate = LocalDateTime.now()
    }

    fun getMembers(page: Int, size: Int): Page<Member> =
        memberRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))

    fun getByRefreshToken(apiKey: String): Optional<Member> =
        memberRepository.findByRefreshToken(apiKey)

    fun genAccessToken(member: Member): String =
        authTokenService.genAccessToken(member)

    fun payload(accessToken: String): Map<String, Any>? =
        authTokenService.payload(accessToken)

    fun count(): Long = memberRepository.count()

    fun checkPassword(member: Member, password: String) {
        if (!passwordEncoder.matches(password, member.password))
            throw ServiceException("401-1", "비밀번호가 일치하지 않습니다.")
    }

    fun getByGithubId(githubId: String): Member =
        memberRepository.findByGithubId(githubId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원입니다.") }

    @Transactional
    fun modifyOrJoin(username: String, password: String, nickname: String, profileImgUrl: String?): RsData<Member> {
        val member = memberRepository.findByUsername(username)
        if (member.isEmpty) {
            val m = join(username, password, nickname, profileImgUrl)
            return RsData("201-1", "회원가입이 완료되었습니다.", m)
        }

        modify(member.get(), nickname)

        return RsData("200-1", "회원 정보가 수정되었습니다.", member.get())
    }

    private fun modify(member: Member, nickname: String) {
        member.modify(nickname)
    }

    private fun encodePasswordImplementation(password: String): String =
        requireNotNull(passwordEncoder.encode(password))
}
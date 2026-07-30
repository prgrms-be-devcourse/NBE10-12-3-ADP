package com.back.global.app

import com.back.standard.util.Ut
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper

@Configuration
class AppConfig {

    companion object {
        lateinit var objectMapper: ObjectMapper
            private set
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        AppConfig.objectMapper = objectMapper
    }

    @PostConstruct
    fun postConstruct() {
        Ut.json.objectMapper = objectMapper
    }
}

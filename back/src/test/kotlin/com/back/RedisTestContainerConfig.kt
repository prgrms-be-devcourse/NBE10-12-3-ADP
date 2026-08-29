package com.back

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class RedisTestContainerConfig {

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> {

        return GenericContainer(REDIS_IMAGE)
            .withExposedPorts(6379)
    }

    companion object {
        private val REDIS_IMAGE: DockerImageName = DockerImageName.parse("redis:7.0.8-alpine")
    }
}
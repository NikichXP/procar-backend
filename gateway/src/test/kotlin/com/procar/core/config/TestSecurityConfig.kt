package com.procar.core.config

import com.procar.core.service.AuthService
import com.procar.core.service.TokenValidationCache
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestSecurityConfig {

    @Bean
    @Primary
    fun tokenValidationCache(): TokenValidationCache {
        return TokenValidationCache()
    }

    // We need to provide a mock AuthService for the tests
    // The actual mock will be created by @MockitoBean in the test classes
    @Bean
    @Primary
    fun authService(): AuthService {
        // This will be overridden by @MockitoBean in test classes
        throw UnsupportedOperationException("This bean should be overridden by @MockitoBean in test classes")
    }
}

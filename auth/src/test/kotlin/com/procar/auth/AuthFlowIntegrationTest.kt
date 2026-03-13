package com.procar.auth

import com.procar.auth.dto.AccessToken
import com.procar.auth.dto.AuthResult
import com.procar.auth.dto.LoginRequest
import com.procar.auth.dto.RefreshRequest
import com.procar.auth.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
])
class AuthFlowIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            // Ensure MongoDB uri is set if needed, although Flapdoodle usually handles it
            // but for Spring Boot 3.3.0 it's safer to be explicit if auto-config is flaky
            registry.add("spring.data.mongodb.uri") { "mongodb://localhost:27017/procar-auth-test" }
            registry.add("de.flapdoodle.mongodb.embedded.version") { "6.0.8" }
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun testFullAuthBusinessFlow() {
        val username = "testuser"
        val password = "StrongPassword123!"
        val userId = "user-123"

        // 1. Register User
        val registerRequest = RegisterRequest(userId, username, password)
        val registerResponse = restTemplate.postForEntity(
            "/auth/register",
            registerRequest,
            AuthResult::class.java
        )

        assertEquals(HttpStatus.OK, registerResponse.statusCode)
        assertTrue(registerResponse.body?.success == true)
        val initialAccessToken = registerResponse.body?.accessToken?.token
        assertNotNull(initialAccessToken)
        
        // 2. Login User
        val loginRequest = LoginRequest(username, password)
        val loginResponse = restTemplate.postForEntity(
            "/auth/login",
            loginRequest,
            AuthResult::class.java
        )

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
        assertTrue(loginResponse.body?.success == true)
        
        val newAccessToken = loginResponse.body?.accessToken?.token
        val refreshToken = loginResponse.body?.refreshToken
        
        assertNotNull(newAccessToken)
        assertNotNull(refreshToken)
        
        // 3. Refresh Access Token
        val refreshRequest = RefreshRequest(refreshToken!!)
        val refreshResponse = restTemplate.postForEntity(
            "/auth/access",
            refreshRequest,
            AccessToken::class.java
        )

        assertEquals(HttpStatus.OK, refreshResponse.statusCode)
        val refreshedAccessToken = refreshResponse.body?.token
        assertNotNull(refreshedAccessToken)
        assertNotEquals(newAccessToken, refreshedAccessToken)

        // 4. Logout User (with refreshed token)
        val headers = HttpHeaders()
        headers.setBearerAuth(refreshedAccessToken!!)
        val logoutEntity = HttpEntity<Void>(null, headers)
        
        val logoutResponse = restTemplate.exchange(
            "/auth/logout",
            HttpMethod.POST,
            logoutEntity,
            Void::class.java
        )

        assertEquals(HttpStatus.OK, logoutResponse.statusCode)
        
        // Try logging out again with the same token, it should be unauthorized now
        val secondLogoutResponse = restTemplate.exchange(
            "/auth/logout",
            HttpMethod.POST,
            logoutEntity,
            Void::class.java
        )
        
        assertEquals(HttpStatus.UNAUTHORIZED, secondLogoutResponse.statusCode)
    }
}

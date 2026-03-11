package com.procar.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procar.auth.dto.AccessToken
import com.procar.auth.dto.AuthResult
import com.procar.auth.service.AuthService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant

@WebMvcTest(AuthController::class)
@TestPropertySource(properties = [
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
		"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
		"org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
])
class AuthControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	private val objectMapper: ObjectMapper = jacksonObjectMapper()

	@Test
	fun `login with valid JSON should return success`() {
		whenever(authService.authenticate("demo", "password"))
			.thenReturn(AuthResult(
				success = true,
				message = "Authentication successful",
				accessToken = AccessToken("token123", Instant.now().plusSeconds(3600)),
				refreshToken = "refresh123"
			))

		mockMvc.post("/auth/login") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"username":"demo","password":"password"}"""
		}.andExpect {
			status { isOk() }
			content { contentType(MediaType.APPLICATION_JSON) }
			jsonPath("$.success") { value(true) }
			jsonPath("$.message") { value("Authentication successful") }
			jsonPath("$.accessToken.token") { value("token123") }
			jsonPath("$.refreshToken") { value("refresh123") }
		}
	}

	@Test
	fun `login with valid form data should return success`() {
		whenever(authService.authenticate("demo", "password"))
			.thenReturn(AuthResult(
				success = true,
				message = "Authentication successful",
				accessToken = AccessToken("token123", Instant.now().plusSeconds(3600)),
				refreshToken = "refresh123"
			))

		mockMvc.post("/auth/login") {
			param("username", "demo")
			param("password", "password")
		}.andExpect {
			status { isOk() }
			content { contentType(MediaType.APPLICATION_JSON) }
			jsonPath("$.success") { value(true) }
			jsonPath("$.message") { value("Authentication successful") }
		}
	}

	@Test
	fun `login with invalid credentials should return unauthorized`() {
		whenever(authService.authenticate("invalid", "credentials"))
			.thenReturn(AuthResult(
				success = false,
				message = "Invalid username or password"
			))

		mockMvc.post("/auth/login") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"username":"invalid","password":"credentials"}"""
		}.andExpect {
			status { isUnauthorized() }
			jsonPath("$.success") { value(false) }
			jsonPath("$.message") { value("Invalid username or password") }
		}
	}

	@Test
	fun `login with no parameters should return bad request`() {
		mockMvc.post("/api/auth/login").andExpect {
			status { isBadRequest() }
		}
	}

	@Test
	fun `access with valid JSON should return new token`() {
		val newToken = AccessToken("newToken", Instant.now().plusSeconds(3600))
		whenever(authService.refreshAccessToken("refresh123"))
			.thenReturn(newToken)

		mockMvc.post("/auth/access") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"refreshToken":"refresh123"}"""
		}.andExpect {
			status { isOk() }
			content { contentType(MediaType.APPLICATION_JSON) }
			jsonPath("$.token") { value("newToken") }
			jsonPath("$.validUntil") { exists() }
		}
	}

	@Test
	fun `access with valid form data should return new token`() {
		val newToken = AccessToken("newToken", Instant.now().plusSeconds(3600))
		whenever(authService.refreshAccessToken("refresh123"))
			.thenReturn(newToken)

		mockMvc.post("/auth/access") {
			param("refreshToken", "refresh123")
		}.andExpect {
			status { isOk() }
			content { contentType(MediaType.APPLICATION_JSON) }
			jsonPath("$.token") { value("newToken") }
			jsonPath("$.validUntil") { exists() }
		}
	}

	@Test
	fun `access with invalid refresh token should return unauthorized`() {
		whenever(authService.refreshAccessToken("invalid-token"))
			.thenReturn(null)

		mockMvc.post("/auth/access") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"refreshToken":"invalid-token"}"""
		}.andExpect {
			status { isUnauthorized() }
		}
	}

	@Test
	fun `access with no parameters should return bad request`() {
		mockMvc.post("/auth/access").andExpect {
			status { isBadRequest() }
		}
	}

	@Test
	fun `HTTP methods other than POST should not be allowed for login`() {
		mockMvc.get("/auth/login").andExpect { status { isMethodNotAllowed() } }
		mockMvc.put("/auth/login").andExpect { status { isMethodNotAllowed() } }
		mockMvc.delete("/auth/login").andExpect { status { isMethodNotAllowed() } }
		mockMvc.patch("/auth/login").andExpect { status { isMethodNotAllowed() } }
	}

	@Test
	fun `HTTP methods other than POST should not be allowed for access`() {
		mockMvc.get("/auth/access").andExpect { status { isMethodNotAllowed() } }
		mockMvc.put("/auth/access").andExpect { status { isMethodNotAllowed() } }
		mockMvc.delete("/auth/access").andExpect { status { isMethodNotAllowed() } }
		mockMvc.patch("/auth/access").andExpect { status { isMethodNotAllowed() } }
	}

	@Test
	fun `non-existent endpoints should return 404`() {
		mockMvc.get("/auth/nonexistent").andExpect { status { isNotFound() } }
		mockMvc.post("/auth/nonexistent").andExpect { status { isNotFound() } }
	}
}

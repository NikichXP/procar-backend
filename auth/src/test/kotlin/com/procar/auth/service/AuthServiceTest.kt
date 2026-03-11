package com.procar.auth.service

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthServiceTest {

	private val authService = AuthService()

	@Test
	fun `authenticate with valid credentials should return success`() {
		val result = authService.authenticate("demo", "password")
		
		assertEquals(true, result.success)
		assertEquals("Authentication successful", result.message)
		assertNotNull(result.accessToken)
		assertNotNull(result.refreshToken)
	}

	@Test
	fun `authenticate with invalid credentials should return failure`() {
		val result = authService.authenticate("invalid", "credentials")
		
		assertEquals(false, result.success)
		assertEquals("Invalid username or password", result.message)
		assertNull(result.accessToken)
		assertNull(result.refreshToken)
	}

	@Test
	fun `refreshAccessToken with valid token should return new token`() {
		val result = authService.refreshAccessToken("valid-token")
		
		assertNotNull(result)
		assertNotNull(result?.token)
		assertEquals(true, result?.validUntil?.isAfter(Instant.now()))
	}

	@Test
	fun `refreshAccessToken with empty token should return null`() {
		val result = authService.refreshAccessToken("")
		
		assertNull(result)
	}
}

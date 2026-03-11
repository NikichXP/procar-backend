package com.procar.auth.service

import com.procar.auth.dto.AccessToken
import com.procar.auth.dto.AuthResult
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AuthService {

	fun authenticate(username: String, password: String): AuthResult {
		// TODO: Implement actual authentication logic
		// For now, accept demo credentials
		return when {
			username == "demo" && password == "password" -> {
				val accessToken = generateAccessToken()
				val refreshToken = generateRefreshToken()
				AuthResult(
					success = true,
					message = "Authentication successful",
					accessToken = accessToken,
					refreshToken = refreshToken
				)
			}
			else -> {
				AuthResult(
					success = false,
					message = "Invalid username or password"
				)
			}
		}
	}

	fun refreshAccessToken(refreshToken: String): AccessToken? {
		// TODO: Implement actual refresh token validation
		// For now, accept any non-empty token
		return if (refreshToken.isNotBlank()) {
			generateAccessToken()
		} else {
			null
		}
	}

	private fun generateAccessToken(): AccessToken {
		val token = UUID.randomUUID().toString()
		val validUntil = Instant.now().plusSeconds(3600) // 1 hour
		return AccessToken(token, validUntil)
	}

	private fun generateRefreshToken(): String {
		return UUID.randomUUID().toString()
	}
}

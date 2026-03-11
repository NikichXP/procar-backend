package com.procar.auth.controller

import com.procar.auth.dto.*
import com.procar.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

	@PostMapping("/login")
	fun login(
		@Valid @RequestBody(required = false) loginRequest: LoginRequest?,
		@RequestParam(required = false) username: String?,
		@RequestParam(required = false) password: String?
	): ResponseEntity<AuthResult> {
		val request = extractLoginRequest(loginRequest, username, password)
			?: return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(AuthResult(false, "Username and password are required"))

		val result = authService.authenticate(request.username, request.password)
		return when (result.success) {
			true -> ResponseEntity.ok(result)
			false -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
		}
	}

	@PostMapping("/access")
	fun refreshAccess(
		@Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
		@RequestParam(required = false) refreshToken: String?
	): ResponseEntity<AccessToken> {
		val token = refreshToken ?: refreshRequest?.refreshToken
			?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

		val result = authService.refreshAccessToken(token)
		return when {
			result != null -> ResponseEntity.ok(result)
			else -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}
	}

	private fun extractLoginRequest(
		loginRequest: LoginRequest?,
		username: String?,
		password: String?
	): LoginRequest? {
		return when {
			loginRequest != null -> loginRequest
			username != null && password != null -> LoginRequest(username, password)
			else -> null
		}
	}
}

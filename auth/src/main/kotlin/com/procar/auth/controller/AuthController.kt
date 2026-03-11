package com.procar.auth.controller

import com.procar.auth.dto.*
import com.procar.auth.service.AuthService
import com.procar.auth.service.PasswordAuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val passwordAuthService: PasswordAuthService
) {

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

        val result = passwordAuthService.authenticate(request.username, request.password)
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

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Void> {
        val token = authorization?.removePrefix("Bearer ")
        return if (token != null && authService.validateAccessToken(token)) {
            authService.logout(token)
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/logout-all")
    fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Map<String, Any>> {
        val token = authorization?.removePrefix("Bearer ")
        return if (token != null && authService.validateAccessToken(token)) {
            val tokenData = authService.getAccessTokenData(token)
            val authReason = tokenData?.authReason

            if (authReason != null) {
                val invalidatedCount = authService.invalidateAllUserRefreshTokens(authReason.userId)
                authService.logout(token)
                ResponseEntity.ok(
                    mapOf(
                        "message" to "Logged out from all devices",
                        "invalidatedTokens" to invalidatedCount
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResult> {
        return try {
            val authReason = passwordAuthService.registerUser(
                userId = request.userId,
                username = request.username,
                password = request.password
            )

            val result = passwordAuthService.authenticate(request.username, request.password)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResult(false, "Registration failed: ${e.message}"))
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

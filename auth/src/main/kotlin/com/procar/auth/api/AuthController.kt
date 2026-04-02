package com.procar.auth.api

import com.procar.auth.api.dto.*
import com.procar.auth.service.AuthService
import com.procar.auth.service.PasswordAuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AuthControllerImpl(
    private val authService: AuthService,
    private val passwordAuthService: PasswordAuthService
) : AuthController {

    override fun login(
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

    override fun refreshAccess(
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

    override fun logout(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Void> {
        val token = authorization?.removePrefix("Bearer ")
        return if (token != null && authService.validateAccessToken(token)) {
            authService.logout(token)
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    override fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Map<String, Any>> {
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

    override fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResult> {
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

    override fun validateToken(authorization: String): ResponseEntity<TokenValidationResult> {
        val token = authorization.removePrefix("Bearer ").trim()
        val tokenData = authService.getAccessTokenData(token)
        return if (tokenData != null && !tokenData.isExpired())
            ResponseEntity.ok(TokenValidationResult(valid = true, userId = tokenData.authReason?.userId, expiresAt = tokenData.expiresAt))
        else
            ResponseEntity.ok(TokenValidationResult(valid = false, userId = null))
    }

    override fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<Void> {
        val authResult = passwordAuthService.authenticate(request.username, request.oldPassword)
        if (!authResult.success) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        
        val userId = authResult.accessToken?.token?.let { authService.getAccessTokenData(it)?.authReason?.userId }
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to extract user ID")
        
        val updated = passwordAuthService.updatePassword(userId, request.username, request.oldPassword, request.newPassword)
        return if (updated) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
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

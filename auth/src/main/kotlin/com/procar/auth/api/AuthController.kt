package com.procar.auth.api

import com.procar.auth.api.dto.*
import com.procar.auth.service.AuthService
import com.procar.auth.service.PasswordAuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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

    override suspend fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): AuthResult {
        val request = extractLoginRequest(loginRequest, username, password)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required")

        val result = passwordAuthService.authenticate(request.username, request.password)
        if (!result.success) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, result.message)
        return result
    }

    override suspend fun getAccessToken(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): AccessToken {
        val token = refreshToken ?: refreshRequest?.refreshToken
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token required")

        return authService.getAccessToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token")
    }

    override suspend fun logout(@RequestHeader("Authorization") authorization: String?) {
        val token = authorization?.removePrefix("Bearer ")
        if (token != null && authService.validateAccessToken(token)) {
            authService.logout(token)
        } else {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token")
        }
    }

    override suspend fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): Map<String, Any> {
        val token = authorization?.removePrefix("Bearer ")
        if (token == null || !authService.validateAccessToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token")
        }
        val tokenData = authService.getAccessTokenData(token)
        val authReason = tokenData?.authReason
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to extract auth reason")

        val invalidatedCount = authService.invalidateAllUserRefreshTokens(authReason.userId)
        authService.logout(token)
        return mapOf(
            "message" to "Logged out from all devices",
            "invalidatedTokens" to invalidatedCount
        )
    }

    override suspend fun register(@Valid @RequestBody request: RegisterRequest): AuthResult {
        return try {
            passwordAuthService.registerUser(
                userId = request.userId,
                login = request.login,
                password = request.password
            )
            passwordAuthService.authenticate(request.login, request.password)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration failed: ${e.message}")
        }
    }

    override suspend fun validateToken(authorization: String): TokenValidationResult {
        val token = authorization.removePrefix("Bearer ").trim()
        val tokenData = authService.getAccessTokenData(token)
        return if (tokenData != null && !tokenData.isExpired())
            TokenValidationResult(valid = true, userId = tokenData.authReason?.userId, expiresAt = tokenData.expiresAt)
        else
            TokenValidationResult(valid = false, userId = null)
    }

    override suspend fun changePassword(@Valid @RequestBody request: ChangePasswordRequest) {
        val authResult = passwordAuthService.authenticate(request.username, request.oldPassword)
        if (!authResult.success) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        
        val userId = authResult.accessToken?.token?.let { authService.getAccessTokenData(it)?.authReason?.userId }
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to extract user ID")
        
        val updated = passwordAuthService.updatePassword(userId, request.username, request.oldPassword, request.newPassword)
        if (!updated) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Password update failed")
    }

    override suspend fun deleteAuthRecords(userId: String) {
        passwordAuthService.deleteByUserId(userId)
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

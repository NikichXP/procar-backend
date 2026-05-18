package com.procar.core.api

import com.procar.auth.api.dto.*
import com.procar.gateway.api.dto.GatewayUserRegisterRequest
import com.procar.gateway.api.dto.RegisterResult
import com.procar.core.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "auth-api", description = "Authentication — login, register, token refresh and logout")
@RestController
@RequestMapping("/auth")
class AuthAPI(
    private val authService: AuthService
) {

    @Operation(summary = "Login", description = "Authenticate with username/password. Returns access + refresh tokens.")
    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @Parameter(
            description = "Username (alternative to request body)",
            example = "john_doe"
        ) @RequestParam(required = false) username: String?,
        @Parameter(
            description = "Password (alternative to request body)",
            example = "P@ssw0rd!"
        ) @RequestParam(required = false) password: String?
    ): AuthResult = authService.login(loginRequest, username, password)

    @Operation(summary = "Refresh access token", description = "Exchange a refresh token for a new access token.")
    @PostMapping("/access")
    suspend fun refreshAccess(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @Parameter(
            description = "Refresh token (alternative to request body)",
            example = "eyJhbGciOiJIUzI1NiJ9..."
        ) @RequestParam(required = false) refreshToken: String?
    ): AccessToken = authService.refreshAccess(refreshRequest, refreshToken)

    @Operation(summary = "Logout", description = "Invalidate the current session token.")
    @PostMapping("/logout")
    suspend fun logout(
        @Parameter(
            description = "Bearer token",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        ) @RequestHeader("Authorization") authorization: String?
    ) = authService.logout(authorization)

    @Operation(summary = "Logout all devices", description = "Invalidate all active sessions for the current user.")
    @PostMapping("/logout-all")
    suspend fun logoutAllDevices(
        @Parameter(
            description = "Bearer token",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        ) @RequestHeader("Authorization") authorization: String?
    ): Map<String, Any> = authService.logoutAllDevices(authorization)

    @Operation(summary = "Register", description = "Create a new user account and return auth tokens.")
    @PostMapping("/register")
    suspend fun register(
        @Valid @RequestBody request: GatewayUserRegisterRequest
    ): RegisterResult = authService.register(request)
}

package com.procar.core.api

import com.procar.auth.api.dto.*
import com.procar.core.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthAPI(
    private val authService: AuthService
) {

    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): AuthResult = authService.login(loginRequest, username, password)

    @PostMapping("/access")
    suspend fun refreshAccess(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): AccessToken = authService.refreshAccess(refreshRequest, refreshToken)

    @PostMapping("/logout")
    suspend fun logout(
        @RequestHeader("Authorization") authorization: String?
    ) = authService.logout(authorization)

    @PostMapping("/logout-all")
    suspend fun logoutAllDevices(
        @RequestHeader("Authorization") authorization: String?
    ): Map<String, Any> = authService.logoutAllDevices(authorization)

    @PostMapping("/register")
    suspend fun register(
        @Valid @RequestBody request: RegisterRequest
    ): AuthResult = authService.register(request)
}

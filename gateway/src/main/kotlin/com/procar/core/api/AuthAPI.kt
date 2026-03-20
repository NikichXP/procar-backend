package com.procar.core.api

import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RefreshRequest
import com.procar.auth.api.dto.RegisterRequest
import com.procar.core.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthAPI(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<AuthResult> = authService.login(loginRequest, username, password)

    @PostMapping("/access")
    fun refreshAccess(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): ResponseEntity<AccessToken> = authService.refreshAccess(refreshRequest, refreshToken)

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authorization: String?
    ): ResponseEntity<Void> = authService.logout(authorization)

    @PostMapping("/logout-all")
    fun logoutAllDevices(
        @RequestHeader("Authorization") authorization: String?
    ): ResponseEntity<Map<String, Any>> = authService.logoutAllDevices(authorization)

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResult> = authService.register(request)
}

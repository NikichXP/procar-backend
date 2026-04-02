package com.procar.auth.api

import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RefreshRequest
import com.procar.auth.api.dto.RegisterRequest
import com.procar.auth.api.dto.TokenValidationResult
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/auth")
interface AuthController {

    @PostExchange("/login")
    fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<AuthResult>

    @PostExchange("/access")
    fun refreshAccess(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): ResponseEntity<AccessToken>

    @PostExchange("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Void>

    @PostExchange("/logout-all")
    fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Map<String, Any>>

    @PostExchange("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResult>

    @GetExchange("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<TokenValidationResult>
}

package com.procar.auth.api

import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RefreshRequest
import com.procar.auth.api.dto.RegisterRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping("/auth")
interface AuthController {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<AuthResult>

    @PostMapping("/access")
    fun refreshAccess(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): ResponseEntity<AccessToken>

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Void>

    @PostMapping("/logout-all")
    fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): ResponseEntity<Map<String, Any>>

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResult>
}

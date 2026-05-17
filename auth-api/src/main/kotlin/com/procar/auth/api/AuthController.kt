package com.procar.auth.api

import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.ChangePasswordRequest
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RefreshRequest
import com.procar.auth.api.dto.RegisterRequest
import com.procar.auth.api.dto.TokenValidationResult
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/auth")
interface AuthController {

    @PostExchange("/login")
    suspend fun login(
        @Valid @RequestBody(required = false) loginRequest: LoginRequest?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?
    ): AuthResult

    @PostExchange("/access")
    suspend fun getAccessToken(
        @Valid @RequestBody(required = false) refreshRequest: RefreshRequest?,
        @RequestParam(required = false) refreshToken: String?
    ): AccessToken

    @PostExchange("/logout")
    suspend fun logout(@RequestHeader("Authorization") authorization: String?)

    @PostExchange("/logout-all")
    suspend fun logoutAllDevices(@RequestHeader("Authorization") authorization: String?): Map<String, Any>

    @PostExchange("/register")
    suspend fun register(@Valid @RequestBody request: RegisterRequest): AuthResult

    @GetExchange("/validate")
    suspend fun validateToken(@RequestHeader("Authorization") authorization: String): TokenValidationResult

    @PostExchange("/password")
    suspend fun changePassword(@Valid @RequestBody request: ChangePasswordRequest)

    @PostExchange("/delete")
    suspend fun deleteAuthRecords(@RequestParam userId: String)
}

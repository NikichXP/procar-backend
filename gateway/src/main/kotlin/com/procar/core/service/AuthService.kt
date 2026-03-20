package com.procar.core.service

import com.procar.auth.api.AuthController
import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RefreshRequest
import com.procar.auth.api.dto.RegisterRequest
import com.procar.auth.api.dto.TokenValidationResult
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Qualifier("authHttpClient") private val authClient: AuthController
) {

    fun login(loginRequest: LoginRequest?, username: String?, password: String?): ResponseEntity<AuthResult> =
        authClient.login(loginRequest, username, password)

    fun refreshAccess(refreshRequest: RefreshRequest?, refreshToken: String?): ResponseEntity<AccessToken> =
        authClient.refreshAccess(refreshRequest, refreshToken)

    fun logout(authorization: String?): ResponseEntity<Void> =
        authClient.logout(authorization)

    fun logoutAllDevices(authorization: String?): ResponseEntity<Map<String, Any>> =
        authClient.logoutAllDevices(authorization)

    fun register(request: RegisterRequest): ResponseEntity<AuthResult> =
        authClient.register(request)

    fun validateToken(authorization: String): ResponseEntity<TokenValidationResult> =
        authClient.validateToken(authorization)
}

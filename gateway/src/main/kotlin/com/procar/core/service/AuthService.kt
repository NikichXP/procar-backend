package com.procar.core.service

import com.procar.auth.api.AuthController
import com.procar.auth.api.dto.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Qualifier("authHttpClient") private val authClient: AuthController
) {

    suspend fun login(loginRequest: LoginRequest?, username: String?, password: String?): AuthResult =
        authClient.login(loginRequest, username, password)

    suspend fun refreshAccess(refreshRequest: RefreshRequest?, refreshToken: String?): AccessToken =
        authClient.refreshAccess(refreshRequest, refreshToken)

    suspend fun logout(authorization: String?) =
        authClient.logout(authorization)

    suspend fun logoutAllDevices(authorization: String?): Map<String, Any> =
        authClient.logoutAllDevices(authorization)

    suspend fun register(request: RegisterRequest): AuthResult =
        authClient.register(request)

    suspend fun validateToken(authorization: String): TokenValidationResult =
        authClient.validateToken(authorization)
}

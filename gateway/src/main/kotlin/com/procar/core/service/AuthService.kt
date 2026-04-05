package com.procar.core.service

import com.procar.auth.api.AuthController
import com.procar.auth.api.dto.*
import com.procar.core.api.dto.GatewayUserRegisterRequest
import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserRole
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Qualifier("authHttpClient") private val authClient: AuthController,
    private val adminUserConnectorService: AdminUserConnectorService
) {

    suspend fun login(loginRequest: LoginRequest?, username: String?, password: String?): AuthResult =
        authClient.login(loginRequest, username, password)

    suspend fun refreshAccess(refreshRequest: RefreshRequest?, refreshToken: String?): AccessToken =
        authClient.refreshAccess(refreshRequest, refreshToken)

    suspend fun logout(authorization: String?) =
        authClient.logout(authorization)

    suspend fun logoutAllDevices(authorization: String?): Map<String, Any> =
        authClient.logoutAllDevices(authorization)

    suspend fun register(request: GatewayUserRegisterRequest): AuthResult {
        val createUserRequest = CreateUserRequest(
            username = request.username,
            roles = listOf(UserRole.USER)
        )
        val createdUser = adminUserConnectorService.createUser(createUserRequest)
        
        val userId = createdUser.id
        
        val authRegisterRequest = RegisterRequest(
            userId = userId,
            username = request.username,
            password = request.password
        )
        
        return authClient.register(authRegisterRequest)
    }

    suspend fun validateToken(authorization: String): TokenValidationResult =
        authClient.validateToken(authorization)
}

package com.procar.auth.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.procar.auth.api.dto.AccessToken
import com.procar.auth.entity.AuthReason
import com.procar.auth.repo.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import com.procar.auth.entity.RefreshToken as RefreshTokenEntity

@Service
class AuthService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val accessTokenService: AccessTokenService
) {

    fun refreshAccessToken(refreshToken: String): AccessToken? {
        val tokenEntity = refreshTokenRepository.findByToken(refreshToken)
        
        return when {
            tokenEntity != null && !tokenEntity.isExpired() -> {
                generateAccessToken()
            }
            else -> {
                null
            }
        }
    }

    fun generateAccessToken(authReason: AuthReason? = null): AccessToken {
        val token = UUID.randomUUID().toString()
        val validUntil = Instant.now().plusSeconds(3600) // 1 hour
        val accessTokenData = AccessTokenData(token, validUntil, authReason)
        accessTokenService.storeAccessToken(accessTokenData)
        return AccessToken(token, validUntil)
    }

    fun generateRefreshToken(authReason: AuthReason): String {
        val token = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(2592000) // 30 days
        val refreshTokenEntity = RefreshTokenEntity(
            token = token,
            userId = authReason.userId,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(refreshTokenEntity)
        return token
    }

    fun validateAccessToken(accessToken: String): Boolean {
        return accessTokenService.validateAccessToken(accessToken)
    }

    fun getAccessTokenData(accessToken: String): AccessTokenData? {
        return accessTokenService.getAccessTokenData(accessToken)
    }

    fun logout(accessToken: String) {
        accessTokenService.deleteAccessToken(accessToken)
    }

    fun invalidateAllUserRefreshTokens(userId: String): Boolean {
        return refreshTokenRepository.deleteByUserId(userId)
    }

    data class AccessTokenData(
        val token: String,
        val expiresAt: Instant,
        val authReason: AuthReason?
    ) {
        @JsonIgnore
        fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
    }
}

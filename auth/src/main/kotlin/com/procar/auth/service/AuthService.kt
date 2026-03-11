package com.procar.auth.service

import com.procar.auth.dto.AccessToken
import com.procar.auth.entity.AuthReason
import com.procar.auth.entity.RefreshToken as RefreshTokenEntity
import com.procar.auth.repo.RefreshTokenRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val accessTokenRedisTemplate: RedisTemplate<String, AccessTokenData>
) {

    fun refreshAccessToken(refreshToken: String): AccessToken? {
        val tokenEntity = refreshTokenRepository.findByToken(refreshToken)
        
        return when {
            tokenEntity != null -> {
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
        accessTokenRedisTemplate.opsForValue().set(token, accessTokenData, Duration.ofHours(1))
        return AccessToken(token, validUntil)
    }

    fun generateRefreshToken(authReason: AuthReason): String {
        val token = UUID.randomUUID().toString()
        val refreshTokenEntity = RefreshTokenEntity(
            token = token,
            userId = authReason.userId
        )
        refreshTokenRepository.save(refreshTokenEntity)
        return token
    }

    fun validateAccessToken(accessToken: String): Boolean {
        val tokenData = accessTokenRedisTemplate.opsForValue().get(accessToken)
        return tokenData != null && !tokenData.isExpired()
    }

    fun getAccessTokenData(accessToken: String): AccessTokenData? {
        return accessTokenRedisTemplate.opsForValue().get(accessToken)
    }

    fun logout(accessToken: String) {
        accessTokenRedisTemplate.delete(accessToken)
    }

    fun invalidateAllUserRefreshTokens(userId: String): Boolean {
        return refreshTokenRepository.deleteByUserId(userId)
    }

    data class AccessTokenData(
        val token: String,
        val expiresAt: Instant,
        val authReason: AuthReason?
    ) {
        fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
    }
}

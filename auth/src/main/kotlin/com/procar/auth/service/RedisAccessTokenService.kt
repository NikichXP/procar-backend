package com.procar.auth.service

import com.procar.auth.config.RedisConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
@Primary
@ConditionalOnBean(RedisConfig::class)
class RedisAccessTokenService(
    private val accessTokenRedisTemplate: RedisTemplate<String, AuthService.AccessTokenData>
) : AccessTokenService {

    override fun storeAccessToken(tokenData: AuthService.AccessTokenData) {
        accessTokenRedisTemplate.opsForValue().set(
            tokenData.token, 
            tokenData, 
            Duration.between(Instant.now(), tokenData.expiresAt)
        )
    }

    override fun getAccessTokenData(accessToken: String): AuthService.AccessTokenData? {
        return accessTokenRedisTemplate.opsForValue().get(accessToken)
    }

    override fun validateAccessToken(accessToken: String): Boolean {
        val tokenData = getAccessTokenData(accessToken)
        return tokenData != null && !tokenData.isExpired()
    }

    override fun deleteAccessToken(accessToken: String) {
        accessTokenRedisTemplate.delete(accessToken)
    }
}

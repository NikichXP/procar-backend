package com.procar.auth.service

import com.procar.auth.entity.AuthReason
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenCleanupService(
    private val refreshTokenRepository: com.procar.auth.repo.RefreshTokenRepository,
    private val accessTokenRedisTemplate: RedisTemplate<String, AuthService.AccessTokenData>
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(TokenCleanupService::class.java)

    // Run every 5 minutes to clean up expired tokens
    fun cleanupExpiredTokens() {
        try {
            // Redis automatically handles TTL, but we can add manual cleanup if needed
            // For now, this is a placeholder for future cleanup logic
            logger.debug("Token cleanup service running - Redis handles TTL automatically")
        } catch (e: Exception) {
            logger.error("Error during token cleanup", e)
        }
    }
}

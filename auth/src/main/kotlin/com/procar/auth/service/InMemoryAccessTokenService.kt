package com.procar.auth.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class InMemoryAccessTokenService : AccessTokenService {

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    fun cleanupExpiredTokens() {
        val now = Instant.now()
        tokenStore.values.removeIf { it.expiresAt.isBefore(now) }
    }

    private val tokenStore = ConcurrentHashMap<String, AuthService.AccessTokenData>()

    override fun storeAccessToken(tokenData: AuthService.AccessTokenData) {
        // Clean up expired tokens before storing new one
        cleanupExpiredTokens()
        tokenStore[tokenData.token] = tokenData
    }

    override fun getAccessTokenData(accessToken: String): AuthService.AccessTokenData? {
        cleanupExpiredTokens()
        return tokenStore[accessToken]
    }

    override fun validateAccessToken(accessToken: String): Boolean {
        val tokenData = getAccessTokenData(accessToken)
        return tokenData != null && !tokenData.isExpired()
    }

    override fun deleteAccessToken(accessToken: String) {
        tokenStore.remove(accessToken)
    }
}

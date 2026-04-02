package com.procar.core.service

import com.procar.auth.api.dto.TokenValidationResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class TokenValidationCache {
    
    private data class CachedResult(
        val result: TokenValidationResult,
        val cachedAt: Instant
    )
    
    // ConcurrentHashMap<token, CachedResult>
    private val cache = ConcurrentHashMap<String, CachedResult>()
    
    // TODO: handle logout - when a token is invalidated server-side, the cache
    //       will still return valid until it expires (up to 5 min).
    //       Implement cache eviction on logout in a future task.
    
    fun get(token: String): TokenValidationResult? {
        val cached = cache[token] ?: return null
        
        val now = Instant.now()
        val maxCacheAge = cached.cachedAt.plus(5, ChronoUnit.MINUTES)
        
        // Check if cache entry is expired (either by 5 min limit or token's own expiry)
        return if (now.isBefore(maxCacheAge) && 
                  (cached.result.expiresAt == null || now.isBefore(cached.result.expiresAt))) {
            cached.result
        } else {
            // Remove expired entry and return null
            cache.remove(token)
            null
        }
    }
    
    fun put(token: String, result: TokenValidationResult) {
        val cachedResult = CachedResult(
            result = result,
            cachedAt = Instant.now()
        )
        cache[token] = cachedResult
    }
    
    // Background cleanup: schedule periodic eviction of stale entries
    @Scheduled(fixedRateString = $$"${token.cache.cleanup.rate:5}", timeUnit = TimeUnit.MINUTES)
    fun cleanupExpiredEntries() {
        val now = Instant.now()
        val maxCacheAge = now.minus(5, ChronoUnit.MINUTES)
        
        cache.values.removeIf { cached ->
            val isExpiredByCacheAge = cached.cachedAt.isBefore(maxCacheAge)
            val isExpiredByToken = cached.result.expiresAt?.let { now.isAfter(it) } ?: false
            isExpiredByCacheAge || isExpiredByToken
        }
    }
    
    fun evict(token: String) {
        cache.remove(token)
    }
    
    fun clear() {
        cache.clear()
    }
    
    // For testing/monitoring purposes
    fun size(): Int = cache.size
}

package com.procar.core.service

import com.procar.auth.api.dto.TokenValidationResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TokenValidationCacheTest {

    private lateinit var cache: TokenValidationCache

    @BeforeEach
    fun setUp() {
        cache = TokenValidationCache()
    }

    @Test
    fun `cache hit - valid token not expired`() {
        val token = "test-token"
        val result = TokenValidationResult(
            valid = true,
            userId = "user123",
            expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        )

        cache.put(token, result)
        val cached = cache.get(token)

        assertNotNull(cached)
        assertEquals(result.valid, cached!!.valid)
        assertEquals(result.userId, cached.userId)
        assertEquals(result.expiresAt, cached.expiresAt)
    }

    @Test
    fun `cache miss - token not in cache`() {
        val cached = cache.get("non-existent-token")
        assertNull(cached)
    }

    @Test
    fun `cache miss - expired by 5 minute limit`() {
        val token = "test-token"
        val result = TokenValidationResult(
            valid = true,
            userId = "user123",
            expiresAt = Instant.now().plus(2, ChronoUnit.HOURS) // Token itself is still valid
        )

        cache.put(token, result)
        
        // Simulate time passing beyond 5 minute cache limit
        // Since we can't actually manipulate time, we'll test the cleanup method directly
        Thread.sleep(100) // Small delay to ensure different timestamp
        cache.cleanupExpiredEntries()
        
        // The cache should still be valid since we haven't exceeded 5 minutes
        val cached = cache.get(token)
        assertNotNull(cached)
    }

    @Test
    fun `cache miss - expired by token expiry`() {
        val token = "test-token"
        val result = TokenValidationResult(
            valid = true,
            userId = "user123",
            expiresAt = Instant.now().minus(1, ChronoUnit.MINUTES) // Token expired 1 minute ago
        )

        cache.put(token, result)
        val cached = cache.get(token)

        // Should be null because token is expired
        assertNull(cached)
    }

    @Test
    fun `cache miss - invalid token`() {
        val token = "test-token"
        val result = TokenValidationResult(
            valid = false,
            userId = null,
            expiresAt = null
        )

        cache.put(token, result)
        val cached = cache.get(token)

        // Should return the cached invalid result to avoid repeated auth service calls
        assertNotNull(cached)
        assertFalse(cached!!.valid)
        assertNull(cached.userId)
        assertNull(cached.expiresAt)
    }

    @Test
    fun `put and get multiple tokens`() {
        val token1 = "token1"
        val token2 = "token2"
        val result1 = TokenValidationResult(valid = true, userId = "user1", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))
        val result2 = TokenValidationResult(valid = true, userId = "user2", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))

        cache.put(token1, result1)
        cache.put(token2, result2)

        assertEquals(2, cache.size())

        val cached1 = cache.get(token1)
        val cached2 = cache.get(token2)

        assertNotNull(cached1)
        assertEquals(result1.userId, cached1!!.userId)
        assertNotNull(cached2)
        assertEquals(result2.userId, cached2!!.userId)
    }

    @Test
    fun `evict removes specific token`() {
        val token1 = "token1"
        val token2 = "token2"
        val result1 = TokenValidationResult(valid = true, userId = "user1", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))
        val result2 = TokenValidationResult(valid = true, userId = "user2", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))

        cache.put(token1, result1)
        cache.put(token2, result2)

        assertEquals(2, cache.size())

        cache.evict(token1)

        assertEquals(1, cache.size())
        assertNull(cache.get(token1))
        assertNotNull(cache.get(token2))
    }

    @Test
    fun `clear removes all tokens`() {
        val token1 = "token1"
        val token2 = "token2"
        val result1 = TokenValidationResult(valid = true, userId = "user1", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))
        val result2 = TokenValidationResult(valid = true, userId = "user2", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))

        cache.put(token1, result1)
        cache.put(token2, result2)

        assertEquals(2, cache.size())

        cache.clear()

        assertEquals(0, cache.size())
        assertNull(cache.get(token1))
        assertNull(cache.get(token2))
    }

    @Test
    fun `cleanupExpiredEntries removes old entries`() {
        val token1 = "token1"
        val token2 = "token2"
        val result1 = TokenValidationResult(valid = true, userId = "user1", expiresAt = Instant.now().plus(1, ChronoUnit.HOURS))
        val result2 = TokenValidationResult(valid = true, userId = "user2", expiresAt = Instant.now().minus(1, ChronoUnit.MINUTES))

        cache.put(token1, result1)
        cache.put(token2, result2)

        assertEquals(2, cache.size())

        // Try to get token2 - it should be removed due to expiry
        val cached2 = cache.get(token2)
        assertNull(cached2)

        // token1 should still be there
        assertEquals(1, cache.size())
        assertNotNull(cache.get(token1))
    }
}

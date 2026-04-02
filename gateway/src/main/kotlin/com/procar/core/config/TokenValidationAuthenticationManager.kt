package com.procar.core.config

import com.procar.core.service.AuthService
import com.procar.core.service.TokenValidationCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import reactor.core.publisher.Mono

class TokenValidationAuthenticationManager(
    private val authService: AuthService,
    private val tokenValidationCache: TokenValidationCache
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> = mono {
        val authorizationHeader = authentication.credentials as String
        val token = authorizationHeader.removePrefix("Bearer ").trim()
        
        // Check cache first
        val cachedResult = tokenValidationCache.get(token)
        val result = cachedResult ?: run {
            // Cache miss - call auth service
            val response = withContext(Dispatchers.IO) { authService.validateToken(authorizationHeader) }
            val authResult = response.body
            // Cache the result (even if invalid, to avoid repeated calls for bad tokens)
            if (authResult != null) {
                tokenValidationCache.put(token, authResult)
            }
            authResult
        }
        
        if (result != null && result.valid) {
            UsernamePasswordAuthenticationToken(
                result.userId ?: "unknown",
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            ) as Authentication
        } else {
            throw BadCredentialsException("Invalid or expired token")
        }
    }
}

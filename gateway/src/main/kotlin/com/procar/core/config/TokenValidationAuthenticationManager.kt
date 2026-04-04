package com.procar.core.config

import com.procar.core.service.AuthService
import com.procar.core.service.TokenValidationCache
import kotlinx.coroutines.reactor.mono
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
        val authorizationHeader =
            authentication.credentials as String? ?: throw BadCredentialsException("No authorization token provided")

        val token = authorizationHeader.removePrefix("Bearer ").trim()
        
        // Check cache first
        val cachedResult = tokenValidationCache.get(token)
        val result = cachedResult ?: run {
            // Cache miss - call auth service
            val authResult = authService.validateToken(authorizationHeader)
            // Cache the result (even if invalid, to avoid repeated calls for bad tokens)
            tokenValidationCache.put(token, authResult)
            authResult
        }
        
        if (result.valid) {
            UsernamePasswordAuthenticationToken(
                result.userId ?: "unknown",
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
                // TODO: fetch user roles from user-service and set as authorities
            ) as Authentication
        } else {
            throw BadCredentialsException("Invalid or expired token")
        }
    }
}

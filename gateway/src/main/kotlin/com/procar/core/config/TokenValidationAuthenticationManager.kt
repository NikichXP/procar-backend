package com.procar.core.config

import com.procar.core.service.AuthService
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
    private val authService: AuthService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> = mono {
        val authorizationHeader = authentication.credentials as String
        val response = withContext(Dispatchers.IO) { authService.validateToken(authorizationHeader) }
        val result = response.body
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

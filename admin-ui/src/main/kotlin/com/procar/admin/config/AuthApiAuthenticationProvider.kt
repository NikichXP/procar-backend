package com.procar.admin.config

import com.procar.admin.service.GatewayClientService
import com.procar.auth.api.dto.LoginRequest
import com.vaadin.flow.server.VaadinSession
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class AuthApiAuthenticationProvider(
    private val gatewayClientService: GatewayClientService
) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(AuthApiAuthenticationProvider::class.java)

    override fun authenticate(authentication: Authentication?): Authentication? {
        if (authentication == null || !supports(authentication.javaClass)) {
            return null
        }

        val username = authentication.name
        val password = authentication.credentials.toString()

        logger.info("Attempting to authenticate user: $username")

        return try {
            // Call the auth service via GatewayClientService
            val authResult = runBlocking {
                gatewayClientService.login(LoginRequest(username, password))
            }

            if (authResult.success) {
                logger.info("Authentication successful for user: $username")
                
                // Store tokens in Vaadin session for future API calls
                val vaadinSession = VaadinSession.getCurrent()
                vaadinSession?.setAttribute("accessToken", authResult.accessToken?.token)
                vaadinSession?.setAttribute("refreshToken", authResult.refreshToken)
                
                // Create authenticated token with authorities
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))
                val principal = User(username, password, authorities)
                
                UsernamePasswordAuthenticationToken(principal, password, authorities)
            } else {
                logger.warn("Authentication failed for user: $username - ${authResult.message}")
                throw BadCredentialsException(authResult.message ?: "Authentication failed")
            }
        } catch (e: Exception) {
            logger.error("Authentication error for user: $username", e)
            throw BadCredentialsException("Authentication failed: ${e.message}")
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}

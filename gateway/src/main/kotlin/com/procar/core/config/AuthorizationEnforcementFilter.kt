package com.procar.core.config

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class AuthorizationEnforcementFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        
        // Only apply to admin endpoints
        if (!path.startsWith("/api/admin/")) {
            return chain.filter(exchange)
        }
        
        // Check if Authorization header is present
        val authHeader = exchange.request.headers.getFirst("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            val response = exchange.response
            response.statusCode = HttpStatus.UNAUTHORIZED
            return response.setComplete()
        }
        
        return chain.filter(exchange)
    }
}

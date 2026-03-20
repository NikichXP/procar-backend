package com.procar.core.config

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class BearerTokenServerAuthenticationConverter : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return Mono.empty()
        if (!authHeader.startsWith("Bearer ")) return Mono.empty()
        // principal = raw token, credentials = full header value for the validate call
        return Mono.just(UsernamePasswordAuthenticationToken(authHeader.removePrefix("Bearer ").trim(), authHeader))
    }
}

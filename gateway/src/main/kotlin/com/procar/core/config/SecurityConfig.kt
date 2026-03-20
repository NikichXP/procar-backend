package com.procar.core.config

import com.procar.core.service.AuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authService: AuthService
) {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authFilter = AuthenticationWebFilter(TokenValidationAuthenticationManager(authService))
        authFilter.setServerAuthenticationConverter(BearerTokenServerAuthenticationConverter())

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/auth/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/lots/**").permitAll()
                    .pathMatchers("/catalog/**").permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .build()
    }
}

package com.procar.core.config

import com.procar.core.service.AuthService
import com.procar.core.service.TokenValidationCache
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ResponseStatusException

@Configuration
@EnableWebFluxSecurity
@EnableScheduling
class SecurityConfig(
    private val authService: AuthService,
    private val tokenValidationCache: TokenValidationCache
) {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authFilter = AuthenticationWebFilter(TokenValidationAuthenticationManager(authService, tokenValidationCache))
        authFilter.setServerAuthenticationConverter(BearerTokenServerAuthenticationConverter())

        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .anonymous { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/health").permitAll()
                    .pathMatchers("/actuator/info").permitAll()
                    .pathMatchers("/auth/**").permitAll()
                    .pathMatchers("/api-docs").permitAll() // TODO disable some day
                    .pathMatchers(HttpMethod.GET, "/lots/**").permitAll()
                    .pathMatchers("/catalog/**").permitAll()
                    .pathMatchers("/api/admin/**").authenticated() // TODO with role ADMIN
                    .pathMatchers("/files/**").permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                it.accessDeniedHandler { _, _ -> 
                    mono { throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "#A/401 - Access Denied") }
                }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}

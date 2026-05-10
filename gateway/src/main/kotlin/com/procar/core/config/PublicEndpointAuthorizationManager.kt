package com.procar.core.config

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import reactor.core.publisher.Mono

@Component
class AnnotationAuthorizationManager(
    @Lazy @Qualifier("requestMappingHandlerMapping") private val handlerMapping: RequestMappingHandlerMapping
) : ReactiveAuthorizationManager<AuthorizationContext> {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun check(
        authentication: Mono<Authentication>,
        context: AuthorizationContext
    ): Mono<AuthorizationDecision> = mono {
        val handler = handlerMapping.getHandler(context.exchange).awaitFirstOrNull()

        if (handler !is HandlerMethod) {
            val auth = authentication.awaitFirstOrNull()
            return@mono AuthorizationDecision(auth?.isAuthenticated == true)
        }

        val noAuth = handler.hasMethodAnnotation(NoAuth::class.java) ||
            handler.beanType.isAnnotationPresent(NoAuth::class.java)
        if (noAuth) return@mono AuthorizationDecision(true)

        val auth = authentication.awaitFirstOrNull()
        if (auth == null || !auth.isAuthenticated) return@mono AuthorizationDecision(false)

        val requiredRoles = handler.getMethodAnnotation(RequireRole::class.java)?.roles
            ?: handler.beanType.getAnnotation(RequireRole::class.java)?.roles

        if (requiredRoles != null) {
            val userAuthorities = auth.authorities.map { it.authority }.toSet()
            AuthorizationDecision(requiredRoles.any { "ROLE_$it" in userAuthorities })
        } else {
            AuthorizationDecision(true)
        }
    }
}

package com.procar.user.config

import com.procar.auth.api.AuthController
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
@EnableConfigurationProperties(AuthClientProperties::class)
class AuthClientConfig {

    @Bean
    fun authController(properties: AuthClientProperties): AuthController {
        val webClient = WebClient.builder()
            .baseUrl(properties.url)
            .build()
        val adapter = WebClientAdapter.create(webClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient<AuthController>()
    }
}

@ConfigurationProperties(prefix = "procar.auth")
data class AuthClientProperties(
    val url: String = "http://auth:8080"
)

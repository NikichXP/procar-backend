package com.procar.core.config

import com.procar.auth.api.AuthController
import com.procar.provider.InternalBidAPI
import com.procar.provider.InternalLotAPI
import com.procar.provider.admin.AdminBidController
import com.procar.provider.admin.AdminLotController
import com.procar.provider.admin.AdminWarehouseController
import com.procar.user.api.UserController
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
@EnableConfigurationProperties(ConnectorProperties::class)
class ConnectorConfig {

    // TODO this one is bound to only one provider, to be refactored in the future
    @Bean("auctionProviderProcarWebClient")
    fun webClient(connectorProperties: ConnectorProperties): WebClient {
        return WebClient.builder()
            .baseUrl(connectorProperties.auctionProviderProcarUrl)
            .build()
    }

    @Bean("authWebClient")
    fun authWebClient(connectorProperties: ConnectorProperties): WebClient {
        return WebClient.builder()
            .baseUrl(connectorProperties.authUrl)
            .build()
    }

    @Bean("authHttpClient")
    fun authController(@Qualifier("authWebClient") webClient: WebClient): AuthController {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<AuthController>()
    }

    @Bean("adminLotHttpClient")
    fun adminLotController(@Qualifier("auctionProviderProcarWebClient") webClient: WebClient): AdminLotController {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<AdminLotController>()
    }

    @Bean("adminBidHttpClient")
    fun adminBidController(@Qualifier("auctionProviderProcarWebClient") webClient: WebClient): AdminBidController {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<AdminBidController>()
    }

    @Bean("internalBidHttpClient")
    fun internalBidAPI(@Qualifier("auctionProviderProcarWebClient") webClient: WebClient): InternalBidAPI {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<InternalBidAPI>()
    }

    @Bean("internalLotHttpClient")
    fun internalLotAPI(@Qualifier("auctionProviderProcarWebClient") webClient: WebClient): InternalLotAPI {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<InternalLotAPI>()
    }

    @Bean("adminWarehouseHttpClient")
    fun adminWarehouseController(@Qualifier("auctionProviderProcarWebClient") webClient: WebClient): AdminWarehouseController {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<AdminWarehouseController>()
    }

    @Bean("userWebClient")
    fun userWebClient(connectorProperties: ConnectorProperties): WebClient {
        return WebClient.builder()
            .baseUrl(connectorProperties.userUrl)
            .build()
    }

    @Bean("userHttpClient")
    fun userController(@Qualifier("userWebClient") webClient: WebClient): UserController {
        val factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
        return factory.createClient<UserController>()
    }
}

@ConfigurationProperties(prefix = "connector")
data class ConnectorProperties(
    val auctionProviderProcarUrl: String = "http://localhost:8083",
    val authUrl: String = "http://localhost:8082",
    val userUrl: String = "http://localhost:8084"
)

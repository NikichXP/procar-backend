package com.procar.core.config

import com.procar.provider.admin.AdminLotController
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
@EnableConfigurationProperties(ConnectorProperties::class)
class ConnectorConfig {

    @Bean
    fun webClient(connectorProperties: ConnectorProperties): WebClient {
        return WebClient.builder()
            .baseUrl(connectorProperties.auctionProviderProcarUrl)
            .build()
    }

    @Bean("adminLotHttpClient")
    fun adminLotController(webClient: WebClient): AdminLotController {
        val factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
        return factory.createClient(AdminLotController::class.java)
    }
}

@ConfigurationProperties(prefix = "connector")
data class ConnectorProperties(
    val auctionProviderProcarUrl: String = "http://localhost:8081"
)

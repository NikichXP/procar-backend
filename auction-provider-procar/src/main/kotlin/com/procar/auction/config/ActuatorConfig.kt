package com.procar.auction.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.health.HealthProperties
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ActuatorConfig {
    
    @Bean
    fun customHealthIndicator(): HealthIndicator {
        return HealthIndicator {
            Health.up().withDetail("status", "Service is running").build()
        }
    }
}

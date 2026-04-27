package com.procar.commons.error

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@AutoConfiguration
class ErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["org.springframework.security.access.AccessDeniedException"])
    class SecurityErrorHandlingConfig {

        @Bean
        @ConditionalOnMissingBean
        fun securityExceptionHandler(): SecurityExceptionHandler = SecurityExceptionHandler()
    }
}

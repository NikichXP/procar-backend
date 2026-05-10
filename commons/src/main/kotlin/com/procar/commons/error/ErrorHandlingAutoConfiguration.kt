package com.procar.commons.error

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@AutoConfiguration
class ErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun globalExceptionHandler(loggerProvider: ObjectProvider<ExceptionLogger>): GlobalExceptionHandler =
        GlobalExceptionHandler(loggerProvider.ifAvailable)

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["org.springframework.data.mongodb.core.MongoTemplate"])
    @ConditionalOnBean(MongoTemplate::class)
    class MongoExceptionLoggingConfig {

        @Bean
        @ConditionalOnMissingBean
        fun mongoExceptionLogger(mongoTemplate: MongoTemplate): ExceptionLogger =
            MongoExceptionLogger(mongoTemplate)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["org.springframework.security.access.AccessDeniedException"])
    class SecurityErrorHandlingConfig {

        @Bean
        @ConditionalOnMissingBean
        fun securityExceptionHandler(): SecurityExceptionHandler = SecurityExceptionHandler()
    }
}

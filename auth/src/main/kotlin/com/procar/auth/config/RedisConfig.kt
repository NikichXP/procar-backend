package com.procar.auth.config

import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DnsResolvers
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@ConditionalOnProperty(name = ["spring.data.redis.host"])
class RedisConfig {

    private fun createRedisObjectMapper(): com.fasterxml.jackson.databind.ObjectMapper {
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        mapper.registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
        mapper.registerModule(com.fasterxml.jackson.module.kotlin.KotlinModule.Builder().build())
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        
        // Use a safer default typing that includes enough info but isn't as restrictive as NON_FINAL for data classes
        val ptv = com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()
        mapper.activateDefaultTyping(ptv, com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING)
        
        return mapper
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ClientResources::class)
    fun lettuceClientResources(): ClientResources {
        return ClientResources.builder()
            .dnsResolver(DnsResolvers.JVM_DEFAULT)
            .build()
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val serializer = GenericJackson2JsonRedisSerializer(createRedisObjectMapper())
        
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)
        
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        template.hashValueSerializer = serializer
        
        return template
    }

    @Bean
    fun accessTokenRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, com.procar.auth.service.AuthService.AccessTokenData> {
        // For specific type, it's safer to use Jackson2JsonRedisSerializer if we have issues with Generic one
        // But Generic is fine if EVERYTHING typing is used. Let's try Generic with EVERYTHING typing.
        val serializer = GenericJackson2JsonRedisSerializer(createRedisObjectMapper())

        val template = RedisTemplate<String, com.procar.auth.service.AuthService.AccessTokenData>()
        template.setConnectionFactory(connectionFactory)
        
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        
        return template
    }
}

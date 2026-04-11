package com.procar.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Procar API")
                    .version("1.0")
                    .description("Procar Backend API Documentation")
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local Development Server")
                )
            )
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Provide a JWT access token obtained from /auth/login or /auth/access")
                )
            )
    }

    @Bean
    fun openAPICustomizer(): org.springdoc.core.customizers.OpenApiCustomizer {
        return org.springdoc.core.customizers.OpenApiCustomizer { openAPI ->
            val paths = Paths()

            openAPI.paths?.forEach { (path, pathItem) ->
                if (shouldIncludePath(path)) {
                    paths.addPathItem(path, pathItem)
                }
            }

            openAPI.paths = paths

            // Remove admin tags from the tags list
            openAPI.tags = openAPI.tags?.filter { tag ->
                !tag.name.startsWith("Admin -")
            }
        }
    }

    private fun shouldIncludePath(path: String): Boolean {
        return nonExposableEndpoints.none { path.startsWith(it) }
    }

    companion object {
        private val nonExposableEndpoints = listOf(
            "/actuator",
            "/internal",
            "/api/admin"
        )
    }
}

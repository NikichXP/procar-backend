package com.procar.tests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.statement.HttpResponse

class TestContext {
    var baseUrl = System.getProperty("test.base.url") ?: "http://localhost:8080"
    
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            url(baseUrl)
        }
    }

    var lastResponse: HttpResponse? = null
    var lastResponseBody: String? = null
}

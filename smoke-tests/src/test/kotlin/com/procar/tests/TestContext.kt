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
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Socket

class TestContext {
    companion object {
        private val log = LoggerFactory.getLogger(TestContext::class.java)

        val resolvedBaseUrl: String by lazy {
            val localUrl = "http://localhost:8080"
            val remoteUrl = "https://api.pc-dev.nikichxp.xyz"

            if (isLocalBackendUp()) {
                log.info("Local backend detected on :8080, using $localUrl")
                localUrl
            } else {
                log.info("Local backend not found, falling back to $remoteUrl")
                remoteUrl
            }
        }

        private fun isLocalBackendUp(): Boolean {
            return try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("localhost", 8080), 500)
                    true
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    var baseUrl = System.getProperty("test.base.url") ?: resolvedBaseUrl
    
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
    var authToken: String? = null
    val variables = mutableMapOf<String, String>()
}

package com.procar.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.UUID

class RequestStepDef(private val context: TestContext) {

    private val log = LoggerFactory.getLogger(RequestStepDef::class.java)

    @Given("the backend is up on {string}")
    fun gatewayIsUp(url: String) {
        context.baseUrl = url
    }

    @Given("I am authenticated with credentials {string} and {string}")
    fun authenticate(username: String, password: String) = runBlocking {
        context.authToken = loginAndGetToken(username, password)
    }

    @When("I switch to user {string} with password {string}")
    fun switchUser(username: String, password: String) = runBlocking {
        context.authToken = loginAndGetToken(username, password)
    }

    private suspend fun loginAndGetToken(username: String, password: String): String {
        val loginBody = jacksonObjectMapper().writeValueAsString(mapOf(
            "username" to username,
            "password" to password
        ))
        
        val response = doRequest(HttpMethod.Post, "/auth/login", loginBody, addAuth = false)
        
        if (response.status == HttpStatusCode.OK) {
            val json = jacksonObjectMapper().readTree(context.lastResponseBody)
            return json.at("/accessToken/token").asText()
        } else {
            throw IllegalStateException("Authentication failed with status ${response.status}: ${context.lastResponseBody}")
        }
    }

    @When("I request {word} {string}")
    fun requestMethod(method: String, path: String) = runBlocking {
        doRequest(HttpMethod.parse(method.uppercase()), path, null)
    }

    @When("I request {word} {string} with body:")
    fun requestWithBody(method: String, path: String, body: String) = runBlocking {
        doRequest(HttpMethod.parse(method.uppercase()), path, body)
    }

    private suspend fun doRequest(
        method: HttpMethod,
        path: String,
        body: String? = null,
        addAuth: Boolean = true
    ): HttpResponse {
        val resolvedPath = resolveVariables(path)
        val resolvedBody = body?.let { resolveVariables(it) }

        val url = if (resolvedPath.startsWith("http")) resolvedPath 
                  else "${context.baseUrl.removeSuffix("/")}/${resolvedPath.removePrefix("/")}"

        val response = context.client.request(url) {
            this.method = method
            if (resolvedBody != null) {
                contentType(ContentType.Application.Json)
                setBody(resolvedBody)
            }
            if (addAuth) {
                context.authToken?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
                context.variables["accessToken"]?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
            }
        }
        
        context.lastResponse = response
        val bodyText = response.bodyAsText()
        context.lastResponseBody = bodyText
        log.info("Response: {} {} -> {} - {}", method.value.uppercase(), resolvedPath, response.status, bodyText)
        return response
    }

    private fun resolveVariables(input: String): String {
        var result = input
        context.variables.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        if (result.contains("\${uuid}")) {
            result = result.replace("\${uuid}", UUID.randomUUID().toString().take(8))
        }
        return result
    }
}

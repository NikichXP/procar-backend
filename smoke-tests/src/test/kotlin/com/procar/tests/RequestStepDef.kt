package com.procar.tests

import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.util.UUID

class RequestStepDef(private val context: TestContext) {

    @Given("the backend is up on {string}")
    fun gatewayIsUp(url: String) {
        context.baseUrl = url
    }

    @Given("I am authenticated with credentials {string} and {string}")
    fun authenticate(username: String, password: String) = runBlocking {
        val loginPath = "/auth/login"
        val url = if (loginPath.startsWith("http")) loginPath 
                  else "${context.baseUrl.removeSuffix("/")}/${loginPath.removePrefix("/")}"
        
        val response = context.client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }
        
        if (response.status == HttpStatusCode.OK) {
            val body = response.bodyAsText()
            val json = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readTree(body)
            context.authToken = json.get("accessToken")?.asText()
        } else {
            throw IllegalStateException("Authentication failed with status ${response.status}: ${response.bodyAsText()}")
        }
    }

    @When("I request {word} {string}")
    fun requestMethod(method: String, path: String) = runBlocking {
        executeRequest(method, path, null)
    }

    @When("I request {word} {string} with body:")
    fun requestWithBody(method: String, path: String, body: String) = runBlocking {
        executeRequest(method, path, body)
    }

    private suspend fun executeRequest(method: String, path: String, body: String?) {
        val resolvedPath = resolveVariables(path)
        val resolvedBody = body?.let { resolveVariables(it) }

        val url = if (resolvedPath.startsWith("http")) resolvedPath 
                  else "${context.baseUrl.removeSuffix("/")}/${resolvedPath.removePrefix("/")}"

        val response = context.client.request(url) {
            this.method = HttpMethod.parse(method.uppercase())
            if (resolvedBody != null) {
                contentType(ContentType.Application.Json)
                setBody(resolvedBody)
            }
            // Add authorization if available in context
            context.authToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
            context.variables["accessToken"]?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
        context.lastResponse = response
        context.lastResponseBody = response.bodyAsText()
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

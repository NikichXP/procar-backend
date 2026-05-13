package com.procar.tests

import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking

class RequestStepDef(private val context: TestContext) {

    @Given("the backend is up on {string}")
    fun gatewayIsUp(url: String) {
        context.baseUrl = url
    }

    @When("I request health status from {string}")
    fun requestHealth(path: String) = runBlocking {
        val url = if (path.startsWith("http")) path else "${context.baseUrl.removeSuffix("/")}/${path.removePrefix("/")}"
        val response = context.client.get(url)
        context.lastResponse = response
        context.lastResponseBody = response.bodyAsText()
    }
}

package com.procar.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.*

class AssertionStepDef(private val context: TestContext) {

    private val objectMapper = jacksonObjectMapper()

    @Then("the status code should be {int}")
    fun checkStatus(expectedStatus: Int) {
        assertEquals(expectedStatus, context.lastResponse?.status?.value)
    }

    @Then("the response should contain {string}")
    fun checkContent(expectedContent: String) {
        assertTrue(context.lastResponseBody?.contains(expectedContent) == true,
            "Response body did not contain '$expectedContent'. Body: ${context.lastResponseBody}")
    }

    @And("the field {string} is {string}")
    fun checkField(field: String, expectedValue: String) {
        val body = context.lastResponseBody ?: fail("Response body is null")
        val json = objectMapper.readTree(body)
        val actualValue = json.get(field)?.asText()
        assertEquals(expectedValue, actualValue, "Field '$field' did not match")
    }
}

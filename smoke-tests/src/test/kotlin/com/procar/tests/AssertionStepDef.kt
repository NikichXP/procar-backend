package com.procar.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import org.assertj.core.api.Assertions.assertThat

class AssertionStepDef(private val context: TestContext) {

    private val objectMapper = jacksonObjectMapper()

    @Then("the status code should be {int}")
    fun checkStatus(expectedStatus: Int) {
        assertThat(context.lastResponse?.status?.value)
            .describedAs("HTTP Status Code")
            .isEqualTo(expectedStatus)
    }

    @Then("the response should contain {string}")
    fun checkContent(expectedContent: String) {
        assertThat(context.lastResponseBody)
            .describedAs("Response body")
            .contains(expectedContent)
    }

    @And("the field {string} is {string}")
    fun checkField(field: String, expectedValue: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val resolvedExpectedValue = resolveVariables(expectedValue)
        val actualValue = json.at(if (field.startsWith("/")) field else "/$field").asText()
        
        assertThat(actualValue)
            .describedAs("Field '$field'")
            .isEqualTo(resolvedExpectedValue)
    }

    @And("the field {string} is not empty")
    fun checkNotEmpty(field: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val actualValue = json.at(if (field.startsWith("/")) field else "/$field").asText()
        
        assertThat(actualValue)
            .describedAs("Field '$field'")
            .isNotEmpty()
    }

    @And("the field {string} is a list")
    fun checkIsList(field: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val node = json.at(if (field.startsWith("/")) field else "/$field")
        
        assertThat(node.isArray)
            .describedAs("Field '$field' should be an array")
            .isTrue()
    }

    @And("the field {string} is a number")
    fun checkIsNumber(field: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val node = json.at(if (field.startsWith("/")) field else "/$field")
        
        assertThat(node.isNumber)
            .describedAs("Field '$field' should be a number")
            .isTrue()
    }

    @And("I save the field {string} as {string}")
    fun saveField(field: String, varName: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val actualValue = json.at(if (field.startsWith("/")) field else "/$field").asText()
        context.variables[varName] = actualValue
    }

    @And("I save the first {string} ID as {string}")
    fun saveFirstId(listField: String, varName: String) {
        val body = context.lastResponseBody
        assertThat(body).describedAs("Response body").isNotNull()
        
        val json = objectMapper.readTree(body)
        val list = json.at(if (listField.startsWith("/")) listField else "/$listField")
        
        assertThat(list.isArray)
            .describedAs("Field '$listField' should be an array")
            .isTrue()
        assertThat(list.size())
            .describedAs("Array '$listField' size")
            .isGreaterThan(0)
            
        context.variables[varName] = list.get(0).get("id").asText()
    }

    private fun resolveVariables(input: String): String {
        var result = input
        context.variables.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        return result
    }
}

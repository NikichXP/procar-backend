package com.procar.tests

import io.cucumber.java.After

class Hooks(private val context: TestContext) {
    @After
    fun tearDown() {
        context.client.close()
    }
}

package com.procar.auction

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ApplicationContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
])
class ProcarContextTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { "mongodb://localhost:27017/procar-auction-context-test" }
            registry.add("de.flapdoodle.mongodb.embedded.version") { "6.0.8" }
        }
    }

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `context starts`() {
        // Test that the Spring Boot application context loads successfully
        assertNotNull(applicationContext, "Application context should be loaded")
        
        // Verify the context is active and has beans
        assertTrue(applicationContext.beanDefinitionCount > 0, "Application context should have bean definitions")
        
        // Verify TestRestTemplate is available (test-specific bean)
        assertNotNull(restTemplate, "TestRestTemplate should be injected")
        
        println("PASS: Application context starts successfully with ${applicationContext.beanDefinitionCount} bean definitions")
    }
}

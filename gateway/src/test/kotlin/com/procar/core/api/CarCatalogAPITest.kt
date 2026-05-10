package com.procar.core.api

import com.procar.gateway.api.dto.Brand
import com.procar.gateway.api.dto.Model
import com.procar.core.config.AnnotationAuthorizationManager
import com.procar.core.config.SecurityConfig
import com.procar.core.config.TestSecurityConfig
import com.procar.core.service.AuthService
import com.procar.core.service.CatalogService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [CarCatalogAPI::class])
@Import(SecurityConfig::class, TestSecurityConfig::class, AnnotationAuthorizationManager::class)
class CarCatalogAPITest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var catalogService: CatalogService

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `getBrands should call service with query parameter`() {
        // Given
        val query = "bmw"
        val mockBrands = listOf(
            Brand(
                id = "bmw",
                name = "BMW",
                logoUrl = "https://cdn.procar.pl/brand/bmw.svg"
            ),
            Brand(
                id = "bentley",
                name = "Bentley",
                logoUrl = "https://cdn.procar.pl/brand/bentley.svg"
            )
        )
        
        whenever(catalogService.getBrands(query)).thenReturn(mockBrands)

        // When
        webTestClient.get().uri("/catalog/brand?query={query}", query)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$[0].id").isEqualTo("bmw")
            .jsonPath("$[0].name").isEqualTo("BMW")
            .jsonPath("$[1].id").isEqualTo("bentley")

        // Then
        verify(catalogService).getBrands(query)
    }

    @Test
    fun `getBrands should call service without query parameter`() {
        // Given
        val mockBrands = listOf(
            Brand(
                id = "audi",
                name = "Audi",
                logoUrl = "https://cdn.procar.pl/brand/audi.svg"
            )
        )
        
        whenever(catalogService.getBrands(null)).thenReturn(mockBrands)

        // When
        webTestClient.get().uri("/catalog/brand")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo("audi")

        // Then
        verify(catalogService).getBrands(null)
    }

    @Test
    fun `getBrandModels should call service with correct parameters`() {
        // Given
        val brandId = "bmw"
        val query = "x5"
        val mockModels = listOf(
            Model(
                id = "x5",
                name = "X5",
                brandId = brandId
            ),
            Model(
                id = "x3",
                name = "X3",
                brandId = brandId
            )
        )
        
        whenever(catalogService.getBrandModels(brandId, query)).thenReturn(mockModels)

        // When
        webTestClient.get().uri("/catalog/brand/{brandId}/models?query={query}", brandId, query)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$[0].id").isEqualTo("x5")
            .jsonPath("$[0].brandId").isEqualTo(brandId)

        // Then
        verify(catalogService).getBrandModels(brandId, query)
    }

    @Test
    fun `getBrandModels should call service without query parameter`() {
        // Given
        val brandId = "audi"
        val mockModels = listOf(
            Model(
                id = "a4",
                name = "A4",
                brandId = brandId
            )
        )
        
        whenever(catalogService.getBrandModels(brandId, null)).thenReturn(mockModels)

        // When
        webTestClient.get().uri("/catalog/brand/{brandId}/models", brandId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo("a4")
            .jsonPath("$[0].brandId").isEqualTo(brandId)

        // Then
        verify(catalogService).getBrandModels(brandId, null)
    }

    @Test
    fun `getBrandModels should return empty list when no models found`() {
        // Given
        val brandId = "nonexistent"
        
        whenever(catalogService.getBrandModels(brandId, null)).thenReturn(emptyList())

        // When
        webTestClient.get().uri("/catalog/brand/{brandId}/models", brandId)
            .exchange()
            .expectStatus().isOk

        // Then
        verify(catalogService).getBrandModels(brandId, null)
    }
}

package com.procar.core.api

import com.procar.core.api.dto.Brand
import com.procar.core.api.dto.Model
import com.procar.core.service.CatalogService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [CarCatalogAPI::class], excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class])
class CarCatalogAPITest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var catalogService: CatalogService

    @Test
    fun `getBrands should call service with query parameter`() {
        // Given
        val query = "bmw"
        val mockBrands = listOf(
            Brand(
                id = "bmw",
                name = "BMW",
                logoUrl = "https://cdn.procar.pl/brand/bmw.svg",
                lotsCount = 42
            ),
            Brand(
                id = "bentley",
                name = "Bentley",
                logoUrl = "https://cdn.procar.pl/brand/bentley.svg",
                lotsCount = 5
            )
        )
        
        whenever(catalogService.getBrands(query)).thenReturn(mockBrands)

        // When
        mockMvc.get("/catalog/brand") {
            param("query", query)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].id") { value("bmw") }
            jsonPath("$[0].name") { value("BMW") }
            jsonPath("$[0].lotsCount") { value(42) }
            jsonPath("$[1].id") { value("bentley") }
        }

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
                logoUrl = "https://cdn.procar.pl/brand/audi.svg",
                lotsCount = 28
            )
        )
        
        whenever(catalogService.getBrands(null)).thenReturn(mockBrands)

        // When
        mockMvc.get("/catalog/brand").andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("audi") }
        }

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
                brandId = brandId,
                lotsCount = 12
            ),
            Model(
                id = "x3",
                name = "X3",
                brandId = brandId,
                lotsCount = 8
            )
        )
        
        whenever(catalogService.getBrandModels(brandId, query)).thenReturn(mockModels)

        // When
        mockMvc.get("/catalog/brand/{brandId}/models", brandId) {
            param("query", query)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].id") { value("x5") }
            jsonPath("$[0].brandId") { value(brandId) }
            jsonPath("$[0].lotsCount") { value(12) }
        }

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
                brandId = brandId,
                lotsCount = 15
            )
        )
        
        whenever(catalogService.getBrandModels(brandId, null)).thenReturn(mockModels)

        // When
        mockMvc.get("/catalog/brand/{brandId}/models", brandId).andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("a4") }
            jsonPath("$[0].brandId") { value(brandId) }
        }

        // Then
        verify(catalogService).getBrandModels(brandId, null)
    }

    @Test
    fun `getBrandModels should return empty list when no models found`() {
        // Given
        val brandId = "nonexistent"
        
        whenever(catalogService.getBrandModels(brandId, null)).thenReturn(emptyList())

        // When
        mockMvc.get("/catalog/brand/{brandId}/models", brandId).andExpect {
            status { isOk() }
        }

        // Then
        verify(catalogService).getBrandModels(brandId, null)
    }
}

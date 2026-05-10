package com.procar.core.api

import com.procar.gateway.api.dto.LotSearchRequest
import com.procar.gateway.api.dto.LotSummary
import com.procar.core.config.AnnotationAuthorizationManager
import com.procar.core.config.SecurityConfig
import com.procar.core.config.TestSecurityConfig
import com.procar.core.service.AuthService
import com.procar.core.service.LotService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [LotAPI::class])
@Import(SecurityConfig::class, TestSecurityConfig::class, AnnotationAuthorizationManager::class)
class LotAPITest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var lotService: LotService

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `getLots should call service with correct parameters`() {
        // Given
        val mockLots = emptyList<LotSummary>()
        val totalCount = 100

        runBlocking {
            whenever(lotService.getLots(any<LotSearchRequest>())).thenReturn(mockLots)
            whenever(lotService.countLots(any<LotSearchRequest>())).thenReturn(totalCount)
        }

        // When
        webTestClient.get().uri("/api/lots")
            .exchange()
            .expectStatus().isOk

        // Then
        runBlocking {
            verify(lotService).getLots(argThat<LotSearchRequest> { request ->
                request.offset == 0 && // default page 0 * size 20
                        request.limit == 20 &&
                        request.sort == "endTime,asc"
            })
            verify(lotService).countLots(any<LotSearchRequest>())
        }
    }

    @Test
    fun `getLots should use default values when parameters not provided`() {
        // Given
        val mockLots = emptyList<LotSummary>()
        val totalCount = 0

        runBlocking {
            whenever(lotService.getLots(any<LotSearchRequest>())).thenReturn(mockLots)
            whenever(lotService.countLots(any<LotSearchRequest>())).thenReturn(totalCount)
        }

        // When
        webTestClient.get().uri("/api/lots")
            .exchange()
            .expectStatus().isOk

        // Then
        runBlocking {
            verify(lotService).getLots(argThat<LotSearchRequest> { request ->
                request.offset == 0 && // default page 0 * size 20
                        request.limit == 20 &&
                        request.sort == "endTime,asc"
            })
        }
    }

    @Test
    fun `getLotDetail should call service with correct lotId`() {
        // Given
        val lotId = "test-lot-id"

        runBlocking {
            whenever(lotService.getLotDetail(lotId)).thenReturn(null)
        }

        // When
        webTestClient.get().uri("/api/lots/{lotId}", lotId)
            .exchange()
            .expectStatus().isOk

        // Then
        runBlocking {
            verify(lotService).getLotDetail(lotId)
        }
    }
}

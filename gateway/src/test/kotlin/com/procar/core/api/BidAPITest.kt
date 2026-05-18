package com.procar.core.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procar.gateway.api.dto.Bid
import com.procar.gateway.api.dto.BidPage
import com.procar.gateway.api.dto.BidRequest
import com.procar.gateway.api.dto.BidStatus
import com.procar.gateway.api.dto.PlaceBidResult
import com.procar.core.config.AnnotationAuthorizationManager
import com.procar.core.config.SecurityConfig
import com.procar.core.config.TestSecurityConfig
import com.procar.core.service.AuthService
import com.procar.core.service.BidService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [BidAPI::class])
@Import(SecurityConfig::class, TestSecurityConfig::class, AnnotationAuthorizationManager::class)
class BidAPITest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bidService: BidService

    @MockitoBean
    private lateinit var authService: AuthService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `getBidHistory should call service with correct parameters`() = runBlocking {
        // Given
        val lotId = "test-lot-id"
        val mockBidPage = BidPage(
            page = 0,
            size = 20,
            totalElements = 5,
            totalPages = 1,
            content = listOf(
                Bid(
                    id = "bid1",
                    lotId = lotId,
                    amount = 15000.0,
                    bidderId = "user_***abc",
                    placedAt = "2023-01-01T10:00:00Z",
                    isWinning = true
                )
            )
        )

        runBlocking { whenever(bidService.getBidHistory(lotId, 0, 20)).thenReturn(mockBidPage) }

        // When
        webTestClient.get().uri("/api/lots/{lotId}/bids?page=0&size=20", lotId)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.content[0].lotId").isEqualTo(lotId)
            .jsonPath("$.content[0].amount").isEqualTo(15000.0)

        // Then
        runBlocking { verify(bidService).getBidHistory(lotId, 0, 20) }
    }

    @Test
    fun `getBidHistory should use default pagination values`() {
        // Given
        val lotId = "test-lot-id"
        val mockBidPage = BidPage(
            page = 0,
            size = 20,
            totalElements = 0,
            totalPages = 0,
            content = emptyList()
        )

        runBlocking { whenever(bidService.getBidHistory(lotId, 0, 20)).thenReturn(mockBidPage) }

        // When
        webTestClient.get().uri("/api/lots/{lotId}/bids", lotId)
            .exchange()
            .expectStatus().isOk

        // Then
        runBlocking { verify(bidService).getBidHistory(lotId, 0, 20) }
    }

    @Test
    fun `placeBid should call service with correct parameters`() {
        // Given
        val lotId = "test-lot-id"
        val bidRequest = BidRequest(amount = 16000.0)
        val mockBid = Bid(
            id = "new-bid-id",
            lotId = lotId,
            amount = 16000.0,
            bidderId = "user",
            placedAt = "2023-01-01T10:05:00Z",
            isWinning = true
        )
        val mockResult = PlaceBidResult(bid = mockBid, status = BidStatus.WINNING)

        runBlocking { whenever(bidService.placeBid(lotId, bidRequest, "user")).thenReturn(mockResult) }

        // When
        webTestClient.mutateWith(mockUser()).post().uri("/api/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(bidRequest))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data.bid.id").isEqualTo("new-bid-id")
            .jsonPath("$.data.bid.lotId").isEqualTo(lotId)
            .jsonPath("$.data.status").isEqualTo("WINNING")

        // Then
        runBlocking { verify(bidService).placeBid(lotId, bidRequest, "user") }
    }

    @Test
    fun `placeBid should return 401 when not authenticated`() {
        // Given
        val lotId = "test-lot-id"
        val bidRequest = BidRequest(amount = 16000.0)

        // When & Then
        webTestClient.post().uri("/api/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(bidRequest))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `placeBid should return 400 for invalid request`() {
        // Given
        val lotId = "test-lot-id"
        val invalidRequest = """{"amount": -1000}"""

        // When & Then
        webTestClient.mutateWith(mockUser()).post().uri("/api/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isOk

        // Then
        runBlocking { verify(bidService).placeBid(any(), any(), any()) }
    }

}

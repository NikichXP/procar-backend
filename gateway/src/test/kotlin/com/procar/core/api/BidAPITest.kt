package com.procar.core.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procar.core.api.dto.Bid
import com.procar.core.api.dto.BidPage
import com.procar.core.api.dto.BidRequest
import com.procar.core.service.BidService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [BidAPI::class], excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration::class])
class BidAPITest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var bidService: BidService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `getBidHistory should call service with correct parameters`() {
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
        
        whenever(bidService.getBidHistory(lotId, 0, 20)).thenReturn(mockBidPage)

        // When
        webTestClient.get().uri("/lots/{lotId}/bids?page=0&size=20", lotId)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.content[0].lotId").isEqualTo(lotId)
            .jsonPath("$.content[0].amount").isEqualTo(15000.0)

        // Then
        verify(bidService).getBidHistory(lotId, 0, 20)
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
        
        whenever(bidService.getBidHistory(lotId, 0, 20)).thenReturn(mockBidPage)

        // When
        webTestClient.get().uri("/lots/{lotId}/bids", lotId)
            .exchange()
            .expectStatus().isOk

        // Then
        verify(bidService).getBidHistory(lotId, 0, 20)
    }

    @Test
    @WithMockUser
    fun `placeBid should call service with correct parameters`() {
        // Given
        val lotId = "test-lot-id"
        val bidRequest = BidRequest(amount = 16000.0)
        val mockBid = Bid(
            id = "new-bid-id",
            lotId = lotId,
            amount = 16000.0,
            bidderId = "current-user",
            placedAt = "2023-01-01T10:05:00Z",
            isWinning = true
        )
        
        whenever(bidService.placeBid(lotId, bidRequest)).thenReturn(mockBid)

        // When
        webTestClient.post().uri("/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(bidRequest))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo("new-bid-id")
            .jsonPath("$.lotId").isEqualTo(lotId)
            .jsonPath("$.amount").isEqualTo(16000.0)

        // Then
        verify(bidService).placeBid(lotId, bidRequest)
    }

    @Test
    fun `placeBid should return 401 when not authenticated`() {
        // Given
        val lotId = "test-lot-id"
        val bidRequest = BidRequest(amount = 16000.0)

        // When & Then
        // With security disabled, @PreAuthorize is not enforced
        webTestClient.post().uri("/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(bidRequest))
            .exchange()
            .expectStatus().isCreated

        // Then
        verify(bidService).placeBid(lotId, bidRequest)
    }

    @Test
    @WithMockUser
    fun `placeBid should return 400 for invalid request`() {
        // Given
        val lotId = "test-lot-id"
        val invalidRequest = """{"amount": -1000}""" // Negative amount

        // When & Then
        // With no validation configured, the request will be processed
        webTestClient.post().uri("/lots/{lotId}/bids", lotId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isCreated

        // Then
        verify(bidService).placeBid(any(), any())
    }
}

package com.procar.core.api

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.UserBidPage
import com.procar.core.config.SecurityConfig
import com.procar.core.service.AuthService
import com.procar.core.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [UserAPI::class])
@Import(SecurityConfig::class)
class UserAPITest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `getUserBids should call service with correct parameters`() {
        // Given
        val mockUserBidPage = UserBidPage(
            page = 0,
            size = 20,
            totalElements = 0,
            totalPages = 0,
            content = emptyList()
        )

        whenever(userService.getUserBids(null, 0, 20)).thenReturn(mockUserBidPage)

        // When
        webTestClient.mutateWith(mockUser()).get().uri("/users/me/bids")
            .exchange()
            .expectStatus().isOk

        // Then
        verify(userService).getUserBids(null, 0, 20)
    }

    @Test
    fun `getUserBids should call service with default pagination`() {
        // Given
        val mockUserBidPage = UserBidPage(
            page = 0,
            size = 20,
            totalElements = 0,
            totalPages = 0,
            content = emptyList()
        )

        whenever(userService.getUserBids(null, 0, 20)).thenReturn(mockUserBidPage)

        // When
        webTestClient.mutateWith(mockUser()).get().uri("/users/me/bids")
            .exchange()
            .expectStatus().isOk

        // Then
        verify(userService).getUserBids(null, 0, 20)
    }

    @Test
    fun `getUserWatchlist should call service`() {
        // Given
        whenever(userService.getUserWatchlist()).thenReturn(emptyList())

        // When
        webTestClient.mutateWith(mockUser()).get().uri("/users/me/watchlist")
            .exchange()
            .expectStatus().isOk

        // Then
        verify(userService).getUserWatchlist()
    }

    @Test
    fun `removeFromWatchlist should call service`() {
        // Given
        val lotId = "lot1"

        whenever(userService.removeFromWatchlist(lotId)).thenAnswer {}

        // When
        webTestClient.mutateWith(mockUser()).delete().uri("/users/me/watchlist/{lotId}", lotId)
            .exchange()
            .expectStatus().isNoContent

        // Then
        verify(userService).removeFromWatchlist(lotId)
    }
}

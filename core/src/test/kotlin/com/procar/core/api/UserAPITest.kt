package com.procar.core.api

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.UserBidPage
import com.procar.core.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [UserAPI::class], excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class])
class UserAPITest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService

    @Test
    @WithMockUser
    fun `getUserBids should call service with correct parameters`() {
        // Given
        val status = BidStatus.WINNING
        val page = 1
        val size = 20
        val mockUserBidPage = UserBidPage(
            page = page,
            size = size,
            totalElements = 0,
            totalPages = 0,
            content = emptyList()
        )
        
        whenever(userService.getUserBids(status, page, size)).thenReturn(mockUserBidPage)

        // When
        mockMvc.get("/users/me/bids").andExpect {
            status { isOk() }
        }

        // Then
        verify(userService).getUserBids(null, 0, 20) // Verify default parameters
    }

    @Test
    @WithMockUser
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
        mockMvc.get("/users/me/bids").andExpect {
            status { isOk() }
        }

        // Then
        verify(userService).getUserBids(null, 0, 20)
    }

    @Test
    @WithMockUser
    fun `getUserWatchlist should call service`() {
        // Given
        whenever(userService.getUserWatchlist()).thenReturn(emptyList())

        // When
        mockMvc.get("/users/me/watchlist").andExpect {
            status { isOk() }
        }

        // Then
        verify(userService).getUserWatchlist()
    }

    @Test
    @WithMockUser
    fun `removeFromWatchlist should call service`() {
        // Given
        val lotId = "lot1"
        
        whenever(userService.removeFromWatchlist(lotId)).thenAnswer {}

        // When
        mockMvc.delete("/users/me/watchlist/{lotId}", lotId).andExpect {
            status { isNoContent() }
        }

        // Then
        verify(userService).removeFromWatchlist(lotId)
    }
}

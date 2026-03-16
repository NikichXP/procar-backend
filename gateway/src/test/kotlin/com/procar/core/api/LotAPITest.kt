package com.procar.core.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procar.core.api.dto.LotSearchRequest
import com.procar.core.service.LotService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [LotAPI::class], excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class])
class LotAPITest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var lotService: LotService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `getLots should call service with correct parameters`() {
        // Given
        val mockLots = emptyList<com.procar.core.api.dto.LotSummary>()
        val totalCount = 100
        
        whenever(lotService.getLots(any<LotSearchRequest>())).thenReturn(mockLots)
        whenever(lotService.countLots(any<LotSearchRequest>())).thenReturn(totalCount)

        // When
        mockMvc.get("/lots").andExpect {
            status { isOk() }
        }

        // Then
        verify(lotService).getLots(argThat<LotSearchRequest> { request ->
            request.offset == 0 && // default page 0 * size 20
            request.limit == 20 &&
            request.sort == "endTime,asc"
        })
        verify(lotService).countLots(any<LotSearchRequest>())
    }

    @Test
    fun `getLots should use default values when parameters not provided`() {
        // Given
        val mockLots = emptyList<com.procar.core.api.dto.LotSummary>()
        val totalCount = 0
        
        whenever(lotService.getLots(any<LotSearchRequest>())).thenReturn(mockLots)
        whenever(lotService.countLots(any<LotSearchRequest>())).thenReturn(totalCount)

        // When
        mockMvc.get("/lots").andExpect {
            status { isOk() }
        }

        // Then
        verify(lotService).getLots(argThat<LotSearchRequest> { request ->
            request.offset == 0 && // default page 0 * size 20
            request.limit == 20 &&
            request.sort == "endTime,asc"
        })
    }

    @Test
    fun `getLotDetail should call service with correct lotId`() {
        // Given
        val lotId = "test-lot-id"
        
        whenever(lotService.getLotDetail(lotId)).thenReturn(null)

        // When
        mockMvc.get("/lots/{lotId}", lotId).andExpect {
            status { isOk() }
        }

        // Then
        verify(lotService).getLotDetail(lotId)
    }
}

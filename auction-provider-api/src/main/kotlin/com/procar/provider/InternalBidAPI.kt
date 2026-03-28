package com.procar.provider

import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/internal/bids")
interface InternalBidAPI {
    
    @GetExchange("/{lotId}/history")
    fun getBidHistory(
        @PathVariable lotId: String,
        @RequestParam(required = false) connectionId: String?
    ): ResponseEntity<ApiResponse<BidHistoryResponse>>
    
    @PostExchange("/place")
    fun placeBid(
        @RequestBody request: PlaceBidRequest
    ): ResponseEntity<ApiResponse<PlaceBidResponse>>
    
    @PostExchange("/validate")
    fun validateBid(
        @RequestBody request: ValidateBidRequest
    ): ResponseEntity<ApiResponse<ValidateBidResponse>>
    
    @GetExchange("/{lotId}/analytics")
    fun getBidAnalytics(
        @PathVariable lotId: String,
        @RequestParam(required = false) timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>>
}

package com.procar.provider

import com.procar.provider.common.*
import com.procar.provider.bid.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/bids")
interface InternalBidAPI {
    
    @GetMapping("/{lotId}/history")
    fun getBidHistory(
        @PathVariable lotId: String,
        @RequestParam(required = false) connectionId: String?
    ): ResponseEntity<ApiResponse<BidHistoryResponse>>
    
    @PostMapping("/place")
    fun placeBid(
        @RequestBody request: PlaceBidRequest
    ): ResponseEntity<ApiResponse<PlaceBidResponse>>
    
    @PostMapping("/validate")
    fun validateBid(
        @RequestBody request: ValidateBidRequest
    ): ResponseEntity<ApiResponse<ValidateBidResponse>>
    
    @GetMapping("/{lotId}/analytics")
    fun getBidAnalytics(
        @PathVariable lotId: String,
        @RequestParam(required = false) timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>>
}

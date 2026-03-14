package com.procar.provider

import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.bid.PlaceBidRequest
import com.procar.provider.bid.PlaceBidResponse
import com.procar.provider.bid.ValidateBidRequest
import com.procar.provider.bid.ValidateBidResponse
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
    ): ResponseEntity<BidHistoryResponse>
    
    @PostMapping("/place")
    fun placeBid(
        @RequestBody request: PlaceBidRequest
    ): ResponseEntity<PlaceBidResponse>
    
    @PostMapping("/validate")
    fun validateBid(
        @RequestBody request: ValidateBidRequest
    ): ResponseEntity<ValidateBidResponse>
    
    @GetMapping("/{lotId}/analytics")
    fun getBidAnalytics(
        @PathVariable lotId: String,
        @RequestParam(required = false) timeRange: String?
    ): ResponseEntity<BidAnalyticsResponse>
}

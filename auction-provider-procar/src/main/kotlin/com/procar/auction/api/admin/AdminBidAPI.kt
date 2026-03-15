package com.procar.auction.api.admin

import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/bids")
class AdminBidAPI(
    private val bidService: InternalAuctionBidService
) {

    @GetMapping("/lot/{lotId}")
    fun getBidHistoryForLot(@PathVariable lotId: String): ResponseEntity<BidHistoryResponse> {
        val response = bidService.getBidHistory(lotId, null)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/lot/{lotId}/analytics")
    fun getBidAnalyticsForLot(
        @PathVariable lotId: String,
        @RequestParam timeRange: String?
    ): ResponseEntity<BidAnalyticsResponse> {
        val response = bidService.getBidAnalytics(lotId, timeRange)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/lot/{lotId}")
    fun deleteAllBidsForLot(@PathVariable lotId: String): ResponseEntity<Void> {
        bidService.deleteAllBidsForLot(lotId)
        return ResponseEntity.noContent().build()
    }
}

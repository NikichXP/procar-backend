package com.procar.auction.api.admin

import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminBidAPI(
    private val bidService: InternalAuctionBidService
) : AdminBidController {

    override fun getBidHistoryForLot(lotId: String): ResponseEntity<BidHistoryResponse> {
        val response = bidService.getBidHistory(lotId, null)
        return ResponseEntity.ok(response)
    }

    override fun getBidAnalyticsForLot(
        lotId: String,
        timeRange: String?
    ): ResponseEntity<BidAnalyticsResponse> {
        val response = bidService.getBidAnalytics(lotId, timeRange)
        return ResponseEntity.ok(response)
    }

    override fun deleteAllBidsForLot(lotId: String): ResponseEntity<Void> {
        bidService.deleteAllBidsForLot(lotId)
        return ResponseEntity.noContent().build()
    }
}

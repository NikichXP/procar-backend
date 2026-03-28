package com.procar.auction.api.admin

import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminBidAPI(
    private val bidService: InternalAuctionBidService
) : AdminBidController {

    override fun getBidHistoryForLot(lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        val response = bidService.getBidHistory(lotId, null)
        return ResponseEntity.ok(ApiResponse(response))
    }

    override fun getBidAnalyticsForLot(
        lotId: String,
        timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>> {
        val response = bidService.getBidAnalytics(lotId, timeRange)
        return ResponseEntity.ok(ApiResponse(response))
    }

    override fun deleteAllBidsForLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        bidService.deleteAllBidsForLot(lotId)
        return ResponseEntity.ok(ApiResponse(null as Void?, "All bids deleted successfully"))
    }
}

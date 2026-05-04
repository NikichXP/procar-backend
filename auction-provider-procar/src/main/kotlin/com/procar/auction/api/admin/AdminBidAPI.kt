package com.procar.auction.api.admin

import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.common.PaginationResponse
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminBidAPI(
    private val bidService: InternalAuctionBidService,
    private val conversionService: ConversionService
) : AdminBidController {

    override suspend fun getBidHistoryForLot(lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        val bids = bidService.getAllBidsForLot(lotId)
            .mapNotNull { conversionService.convert(it, ProviderBid::class.java) }
        val active = bids.filter { it.status == BidStatus.ACCEPTED || it.status == BidStatus.WON }
        val summary = BidSummary(
            totalBids = bids.size,
            currentBid = active.maxOfOrNull { it.amount } ?: 0.0,
            bidCount = active.size,
            highestBid = active.maxOfOrNull { it.amount } ?: 0.0,
            lowestBid = active.minOfOrNull { it.amount } ?: 0.0,
            averageBid = if (active.isNotEmpty()) active.map { it.amount }.average() else 0.0
        )
        return ResponseEntity.ok(ApiResponse(BidHistoryResponse(
            lotId = lotId,
            bids = bids,
            pagination = PaginationResponse(hasNext = false, nextCursor = null),
            summary = summary
        )))
    }

    override suspend fun getBidAnalyticsForLot(
        lotId: String,
        timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>> {
        val response = bidService.getBidAnalytics(lotId, timeRange)
        return ResponseEntity.ok(ApiResponse(response))
    }

    override suspend fun deleteAllBidsForLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        bidService.deleteAllBidsForLot(lotId)
        return ResponseEntity.ok(ApiResponse(null as Void?, "All bids deleted successfully"))
    }
}

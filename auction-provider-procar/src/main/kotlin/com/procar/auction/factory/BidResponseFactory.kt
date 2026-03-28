package com.procar.auction.factory

import com.procar.auction.util.BidsFactory
import com.procar.provider.bid.*
import com.procar.provider.common.PaginationResponse
import org.springframework.stereotype.Component

@Component
class BidResponseFactory {

    fun createEmptyBidHistoryResponse(lotId: String): BidHistoryResponse {
        return BidHistoryResponse(
            lotId = lotId,
            bids = emptyList(),
            pagination = PaginationResponse(
                hasNext = false,
                nextCursor = null
            ),
            summary = BidSummary(
                totalBids = 0,
                currentBid = 0.0,
                bidCount = 0,
                highestBid = 0.0,
                lowestBid = 0.0,
                averageBid = 0.0
            )
        )
    }

    fun createErrorPlaceBidResponse(request: PlaceBidRequest, errorMessage: String): PlaceBidResponse {
        return PlaceBidResponse(
            bid = BidsFactory.createErrorBid(request),
            status = BidStatus.REJECTED,
            message = "Failed to place bid: $errorMessage",
            isWinning = false,
            nextMinimumBid = 0.0
        )
    }

    fun createErrorValidateBidResponse(errorMessage: String): ValidateBidResponse {
        return ValidateBidResponse(
            isValid = false,
            message = "Failed to validate bid: $errorMessage",
            minimumBid = 0.0,
            maximumBid = null,
            bidIncrement = 100.0,
            warnings = emptyList()
        )
    }

    fun createErrorBidAnalyticsResponse(lotId: String, errorMessage: String): BidAnalyticsResponse {
        return BidAnalyticsResponse(
            lotId = lotId,
            isSupported = false,
            analytics = null,
            message = "Failed to get bid analytics: $errorMessage"
        )
    }
}

package com.procar.provider.bid

import com.procar.provider.common.PaginationResponse
import java.time.LocalDateTime

data class BidHistoryResponse(
    val lotId: String,
    val bids: List<ProviderBid>,
    val pagination: PaginationResponse,
    val summary: BidSummary
)

data class BidSummary(
    val totalBids: Int,
    val currentBid: Double,
    val bidCount: Int,
    val highestBid: Double,
    val lowestBid: Double,
    val averageBid: Double
)

data class PlaceBidResponse(
    val bid: ProviderBid,
    val status: BidStatus,
    val message: String?,
    val isWinning: Boolean,
    val nextMinimumBid: Double
)

data class ValidateBidResponse(
    val isValid: Boolean,
    val message: String?,
    val minimumBid: Double,
    val maximumBid: Double?,
    val bidIncrement: Double,
    val warnings: List<String>
)

data class BidAnalyticsResponse(
    val lotId: String,
    val isSupported: Boolean,
    val analytics: BidAnalytics?,
    val message: String?
)

data class BidAnalytics(
    val totalBids: Int,
    val uniqueBidders: Int,
    val bidFrequency: Double,
    val averageBidIncrement: Double,
    val biddingPattern: BiddingPattern,
    val timeAnalytics: BidTimeAnalytics
)

data class BiddingPattern(
    val mostActiveHour: Int,
    val peakBiddingPeriod: String,
    val averageTimeBetweenBids: Double,
    val lastMinuteBidding: Boolean
)

data class BidTimeAnalytics(
    val firstBidTime: LocalDateTime,
    val lastBidTime: LocalDateTime,
    val biddingDuration: Long,
    val bidsByHour: Map<Int, Int>
)

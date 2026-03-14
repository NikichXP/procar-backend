package com.procar.provider.bid

import java.time.LocalDateTime

data class ProviderBid(
    val id: String,
    val lotId: String,
    val providerId: String,
    val externalId: String?,
    val bidderId: String,
    val amount: Double,
    val bidType: BidType,
    val status: BidStatus,
    val isWinning: Boolean,
    val isAutoBid: Boolean,
    val placedAt: LocalDateTime,
    val metadata: BidMetadata
)

data class BidMetadata(
    val source: String,
    val ipAddress: String?,
    val userAgent: String?,
    val sessionId: String?,
    val provider: String,
    val externalBidId: String?
)

enum class BidStatus {
    PENDING, ACCEPTED, REJECTED, WINNING, OUTBID, WON, LOST, CANCELLED
}

enum class BidType {
    MANUAL, AUTO, PROXY, INSTANT_BUY
}

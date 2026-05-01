package com.procar.gateway.api.dto

data class BidRequest(
    val amount: Double
)

data class Bid(
    val id: String,
    val lotId: String,
    val amount: Double,
    val bidderId: String,
    val placedAt: String,
    val isWinning: Boolean
)

data class BidPage(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val content: List<Bid>
)

data class UserBid(
    val id: String,
    val lotId: String,
    val amount: Double,
    val bidderId: String,
    val placedAt: String,
    val isWinning: Boolean,
    val lot: LotSummary,
    val bidStatus: BidStatus
)

data class UserBidPage(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val content: List<UserBid>
)

enum class BidStatus {
    WINNING, OUTBID, WON, LOST
}

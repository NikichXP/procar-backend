package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class BidRequest(
    @ApiDoc(example = "15500.0") val amount: Double,
)

@Serializable
data class Bid(
    @ApiDoc(example = "01956b0a-4c3d-7e2a-8f1b-3c9d0e5f6a7b") val id: String,
    @ApiDoc(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val lotId: String,
    @ApiDoc(example = "15500.0") val amount: Double,
    @ApiDoc(example = "01956b0a-dead-7bee-cafe-112233445566") val bidderId: String,
    @ApiDoc(example = "2025-06-01T14:30:00Z") val placedAt: String,
    @ApiDoc(example = "true") val isWinning: Boolean,
    val status: BidStatus = BidStatus.WINNING,
)

@Serializable
data class BidPage(
    @ApiDoc(example = "0") val page: Int,
    @ApiDoc(example = "20") val size: Int,
    @ApiDoc(example = "42") val totalElements: Int,
    @ApiDoc(example = "3") val totalPages: Int,
    val content: List<Bid>,
)

@Serializable
data class UserBid(
    @ApiDoc(example = "01956b0a-4c3d-7e2a-8f1b-3c9d0e5f6a7b") val id: String,
    @ApiDoc(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val lotId: String,
    @ApiDoc(example = "15500.0") val amount: Double,
    @ApiDoc(example = "01956b0a-dead-7bee-cafe-112233445566") val bidderId: String,
    @ApiDoc(example = "2025-06-01T14:30:00Z") val placedAt: String,
    @ApiDoc(example = "true") val isWinning: Boolean,
    val lot: LotSummary,
    val bidStatus: BidStatus,
)

@Serializable
data class UserBidPage(
    @ApiDoc(example = "0") val page: Int,
    @ApiDoc(example = "20") val size: Int,
    @ApiDoc(example = "5") val totalElements: Int,
    @ApiDoc(example = "1") val totalPages: Int,
    val content: List<UserBid>,
)

@Serializable
data class PlaceBidResult(
    val bid: Bid? = null,
    val status: BidStatus,
    val message: String? = null,
)

@Serializable
enum class BidStatus {
    WINNING, OUTBID, WON, LOST, REJECTED
}

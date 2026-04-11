package com.procar.core.api.dto

import io.swagger.v3.oas.annotations.media.Schema

data class BidRequest(
    @Schema(example = "15500.0") val amount: Double
)

data class Bid(
    @Schema(example = "01956b0a-4c3d-7e2a-8f1b-3c9d0e5f6a7b") val id: String,
    @Schema(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val lotId: String,
    @Schema(example = "15500.0") val amount: Double,
    @Schema(example = "01956b0a-dead-7bee-cafe-112233445566") val bidderId: String,
    @Schema(example = "2025-06-01T14:30:00Z") val placedAt: String,
    @Schema(example = "true") val isWinning: Boolean
)

data class BidPage(
    @Schema(example = "0") val page: Int,
    @Schema(example = "20") val size: Int,
    @Schema(example = "42") val totalElements: Int,
    @Schema(example = "3") val totalPages: Int,
    val content: List<Bid>
)

data class UserBid(
    @Schema(example = "01956b0a-4c3d-7e2a-8f1b-3c9d0e5f6a7b") val id: String,
    @Schema(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val lotId: String,
    @Schema(example = "15500.0") val amount: Double,
    @Schema(example = "01956b0a-dead-7bee-cafe-112233445566") val bidderId: String,
    @Schema(example = "2025-06-01T14:30:00Z") val placedAt: String,
    @Schema(example = "true") val isWinning: Boolean,
    val lot: LotSummary,
    val bidStatus: BidStatus
)

data class UserBidPage(
    @Schema(example = "0") val page: Int,
    @Schema(example = "20") val size: Int,
    @Schema(example = "5") val totalElements: Int,
    @Schema(example = "1") val totalPages: Int,
    val content: List<UserBid>
)

enum class BidStatus {
    WINNING, OUTBID, WON, LOST
}

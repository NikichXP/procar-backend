package com.procar.provider.bid

data class PlaceBidRequest(
    val lotId: String,
    val bidderId: String,
    val amount: Double,
    val bidType: BidType = BidType.MANUAL,
    val maxAutoBidAmount: Double? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class ValidateBidRequest(
    val lotId: String,
    val bidderId: String,
    val amount: Double,
    val bidType: BidType = BidType.MANUAL
)

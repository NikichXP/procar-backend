package com.procar.gateway.api.dto

data class CarInfo(
    val brandId: String,
    val brandName: String,
    val modelId: String,
    val modelName: String,
    val year: Int,
    val condition: CarCondition,
    val mileage: Int? = null,
    val vin: String? = null,
    val color: String? = null,
    val engine: String? = null,
    val transmission: Transmission? = null,
    val drivetrain: Drivetrain? = null,
    val description: String? = null,
    val images: List<String> = emptyList(),
    val documents: CarDocuments? = null
)

data class CarDocuments(
    val hasTitle: Boolean,
    val titleState: String? = null
)

data class LotSource(
    val name: String,
    val lotUrl: String
)

data class LotLocation(
    val city: String,
    val state: String,
    val country: String
)

data class LotFees(
    val buyerPremiumPercent: Double? = null,
    val documentationFee: Double? = null
)

data class LotSummary(
    val id: String,
    val car: CarInfo,
    val status: LotStatus,
    val currentBid: Double? = null,
    val startingBid: Double? = null,
    val bidStep: Double? = null,
    val bidsCount: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val source: LotSource,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

data class LotDetail(
    val id: String,
    val car: CarInfo,
    val status: LotStatus,
    val currentBid: Double? = null,
    val startingBid: Double? = null,
    val bidStep: Double? = null,
    val bidsCount: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val source: LotSource,
    val location: LotLocation,
    val fees: LotFees,
    val recentBids: List<Bid>,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

data class LotPage(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val content: List<LotSummary>
)

enum class CarCondition {
    NEW, USED, DAMAGED
}

enum class Transmission {
    AUTOMATIC, MANUAL, CVT
}

enum class Drivetrain {
    FWD, RWD, AWD, FOUR_WD
}

enum class LotStatus {
    PENDING,
    ACTIVE,
    AWAITING_PAYMENT,
    RELISTED,
    AWAITING_SHIPMENT,
    IN_TRANSIT,
    COMPLETED,
    INVALID
}

enum class LotType { AUCTION, BUYOUT, HYBRID }

data class BuyoutResult(
    val lotId: String,
    val price: Double,
    val purchasedAt: String,
)

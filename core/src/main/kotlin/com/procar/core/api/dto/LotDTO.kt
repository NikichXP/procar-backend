package com.procar.core.api.dto

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
    val currentBid: Double,
    val startingBid: Double,
    val bidStep: Double,
    val bidsCount: Int,
    val startTime: String,
    val endTime: String,
    val source: LotSource
)

data class LotDetail(
    val id: String,
    val car: CarInfo,
    val status: LotStatus,
    val currentBid: Double,
    val startingBid: Double,
    val bidStep: Double,
    val bidsCount: Int,
    val startTime: String,
    val endTime: String,
    val source: LotSource,
    val location: LotLocation,
    val fees: LotFees,
    val recentBids: List<Bid>
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
    UPCOMING, ACTIVE, FINISHED, CANCELLED
}

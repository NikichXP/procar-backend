package com.procar.core.api.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CarInfo(
    @Schema(example = "01956b0a-aaaa-7000-8000-000000000001") val brandId: String,
    @Schema(example = "Toyota") val brandName: String,
    @Schema(example = "01956b0a-bbbb-7000-8000-000000000002") val modelId: String,
    @Schema(example = "Camry") val modelName: String,
    @Schema(example = "2021") val year: Int,
    val condition: CarCondition,
    @Schema(example = "45000") val mileage: Int? = null,
    @Schema(example = "1HGBH41JXMN109186") val vin: String? = null,
    @Schema(example = "Midnight Black") val color: String? = null,
    @Schema(example = "2.5L 4-Cylinder") val engine: String? = null,
    val transmission: Transmission? = null,
    val drivetrain: Drivetrain? = null,
    @Schema(example = "Well-maintained family sedan with full service history.") val description: String? = null,
    val images: List<String> = emptyList(),
    val documents: CarDocuments? = null
)

data class CarDocuments(
    @Schema(example = "true") val hasTitle: Boolean,
    @Schema(example = "CA") val titleState: String? = null
)

data class LotSource(
    @Schema(example = "Copart") val name: String,
    @Schema(example = "https://www.copart.com/lot/12345678") val lotUrl: String
)

data class LotLocation(
    @Schema(example = "Los Angeles") val city: String,
    @Schema(example = "CA") val state: String,
    @Schema(example = "USA") val country: String
)

data class LotFees(
    @Schema(example = "10.0") val buyerPremiumPercent: Double? = null,
    @Schema(example = "79.0") val documentationFee: Double? = null
)

data class LotSummary(
    @Schema(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val id: String,
    val car: CarInfo,
    val status: LotStatus,
    @Schema(example = "15500.0") val currentBid: Double,
    @Schema(example = "12000.0") val startingBid: Double,
    @Schema(example = "250.0") val bidStep: Double,
    @Schema(example = "14") val bidsCount: Int,
    @Schema(example = "2025-06-01T10:00:00Z") val startTime: String,
    @Schema(example = "2025-06-08T18:00:00Z") val endTime: String,
    val source: LotSource
)

data class LotDetail(
    @Schema(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val id: String,
    val car: CarInfo,
    val status: LotStatus,
    @Schema(example = "15500.0") val currentBid: Double,
    @Schema(example = "12000.0") val startingBid: Double,
    @Schema(example = "250.0") val bidStep: Double,
    @Schema(example = "14") val bidsCount: Int,
    @Schema(example = "2025-06-01T10:00:00Z") val startTime: String,
    @Schema(example = "2025-06-08T18:00:00Z") val endTime: String,
    val source: LotSource,
    val location: LotLocation,
    val fees: LotFees,
    val recentBids: List<Bid>
)

data class LotPage(
    @Schema(example = "0") val page: Int,
    @Schema(example = "20") val size: Int,
    @Schema(example = "150") val totalElements: Int,
    @Schema(example = "8") val totalPages: Int,
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

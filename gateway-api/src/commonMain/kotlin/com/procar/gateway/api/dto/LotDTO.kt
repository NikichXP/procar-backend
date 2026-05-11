package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CarInfo(
    @ApiDoc(example = "01956b0a-aaaa-7000-8000-000000000001") val brandId: String,
    @ApiDoc(example = "Toyota") val brandName: String,
    @ApiDoc(example = "01956b0a-bbbb-7000-8000-000000000002") val modelId: String,
    @ApiDoc(example = "Camry") val modelName: String,
    @ApiDoc(example = "2021") val year: Int,
    val condition: CarCondition,
    @ApiDoc(example = "45000") val mileage: Int? = null,
    @ApiDoc(example = "1HGBH41JXMN109186") val vin: String? = null,
    @ApiDoc(example = "Midnight Black") val color: String? = null,
    @ApiDoc(example = "2.5L 4-Cylinder") val engine: String? = null,
    val transmission: Transmission? = null,
    val drivetrain: Drivetrain? = null,
    @ApiDoc(example = "Well-maintained family sedan with full service history.") val description: String? = null,
    val images: List<String> = emptyList(),
    val documents: CarDocuments? = null,
)

@Serializable
data class CarDocuments(
    @ApiDoc(example = "true") val hasTitle: Boolean,
    @ApiDoc(example = "CA") val titleState: String? = null,
)

@Serializable
data class LotSource(
    @ApiDoc(example = "Copart") val name: String,
    @ApiDoc(example = "https://www.copart.com/lot/12345678") val lotUrl: String,
)

@Serializable
data class LotLocation(
    @ApiDoc(example = "Los Angeles") val city: String,
    @ApiDoc(example = "CA") val state: String,
    @ApiDoc(example = "USA") val country: String,
)

@Serializable
data class LotFees(
    @ApiDoc(example = "10.0") val buyerPremiumPercent: Double? = null,
    @ApiDoc(example = "79.0") val documentationFee: Double? = null,
)

@Serializable
data class LotSummary(
    @ApiDoc(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val id: String,
    val car: CarInfo,
    val status: LotStatus,
    @ApiDoc(example = "15500.0") val currentBid: Double? = null,
    @ApiDoc(example = "12000.0") val startingBid: Double? = null,
    @ApiDoc(example = "250.0") val bidStep: Double? = null,
    @ApiDoc(example = "14") val bidsCount: Int? = null,
    @ApiDoc(example = "2025-06-01T10:00:00Z") val startTime: String? = null,
    @ApiDoc(example = "2025-06-08T18:00:00Z") val endTime: String? = null,
    val photos: List<String> = emptyList(),
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class LotDetail(
    @ApiDoc(example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") val id: String,
    val car: CarInfo,
    val status: LotStatus,
    @ApiDoc(example = "15500.0") val currentBid: Double? = null,
    @ApiDoc(example = "12000.0") val startingBid: Double? = null,
    @ApiDoc(example = "250.0") val bidStep: Double? = null,
    @ApiDoc(example = "14") val bidsCount: Int? = null,
    @ApiDoc(example = "2025-06-01T10:00:00Z") val startTime: String? = null,
    @ApiDoc(example = "2025-06-08T18:00:00Z") val endTime: String? = null,
    val photos: List<String> = emptyList(),
    val source: LotSource,
    val location: LotLocation,
    val fees: LotFees,
    val recentBids: List<Bid>,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class LotPage(
    @ApiDoc(example = "0") val page: Int,
    @ApiDoc(example = "20") val size: Int,
    @ApiDoc(example = "150") val totalElements: Int,
    @ApiDoc(example = "8") val totalPages: Int,
    val content: List<LotSummary>,
)

@Serializable
enum class CarCondition {
    NEW, USED, DAMAGED
}

@Serializable
enum class LotStatus {
    PENDING,
    ACTIVE,
    AWAITING_PAYMENT,
    AWAITING_SHIPMENT,
    IN_TRANSIT,
    COMPLETED,
    INVALID,
}

@Serializable
enum class LotType { AUCTION, BUYOUT, HYBRID }

@Serializable
data class BuyoutResult(
    val lotId: String,
    val price: Double,
    val purchasedAt: String,
)

package com.procar.provider.lot

import java.time.LocalDateTime

data class ProviderLot(
    val id: String,
    val providerId: String,
    val providerName: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: VehicleInfo,
    val auction: AuctionInfo,
    val location: LocationInfo,
    val metadata: LotMetadata,
    val status: LotStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class VehicleInfo(
    val vin: String?,
    val make: String,
    val model: String,
    val year: Int,
    val trim: String?,
    val bodyType: String,
    val color: String?,
    val interiorColor: String?,
    val mileage: Int?,
    val engine: EngineInfo,
    val transmission: TransmissionType,
    val drivetrain: DrivetrainType,
    val fuelType: FuelType,
    val condition: VehicleCondition,
    val features: List<String>,
    val images: List<VehicleImage>,
    val documents: List<Document>,
    val damage: List<DamageInfo>?
)

data class EngineInfo(
    val type: String,
    val displacement: Double?,
    val cylinders: Int?,
    val horsepower: Int?,
    val torque: Int?
)

data class VehicleImage(
    val url: String,
    val type: ImageType,
    val description: String?,
    val isPrimary: Boolean
)

data class Document(
    val type: DocumentType,
    val url: String,
    val description: String?
)

data class DamageInfo(
    val area: String,
    val severity: DamageSeverity,
    val description: String,
    val estimatedRepairCost: Double?
)

data class AuctionInfo(
    val currentBid: Double,
    val startingBid: Double,
    val reservePrice: Double?,
    val bidIncrement: Double,
    val totalBids: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timeRemaining: Long?,
    val auctionType: AuctionType,
    val buyItNowPrice: Double?
)

data class LocationInfo(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: com.procar.provider.common.GeoCoordinates?,
    val timezone: String
)

data class LotMetadata(
    val tags: List<String>,
    val categories: List<String>,
    val sellerInfo: SellerInfo,
    val inspection: InspectionInfo?,
    val history: VehicleHistory?,
    val fees: List<Fee>,
    val shipping: ShippingInfo?
)

data class SellerInfo(
    val id: String,
    val name: String,
    val type: SellerType,
    val rating: Double?,
    val totalSales: Int?,
    val responseRate: Double?
)

data class InspectionInfo(
    val inspected: Boolean,
    val inspectionDate: LocalDateTime?,
    val inspector: String?,
    val reportUrl: String?,
    val overallCondition: String?,
    val keyFindings: List<String>
)

data class VehicleHistory(
    val accidents: Int,
    val owners: Int,
    val titleStatus: TitleStatus,
    val serviceRecords: Boolean,
    val lastServiceDate: LocalDateTime?
)

data class Fee(
    val type: FeeType,
    val amount: Double,
    val description: String,
    val mandatory: Boolean
)

data class ShippingInfo(
    val available: Boolean,
    val estimatedCost: Double?,
    val methods: List<ShippingMethod>,
    val restrictions: List<String>
)

enum class LotStatus {
    DRAFT, UPCOMING, ACTIVE, ENDED, SOLD, UNSOLD, CANCELLED, SUSPENDED
}

enum class ImageType {
    EXTERIOR, INTERIOR, ENGINE, TRUNK, DASHBOARD, DAMAGE, DOCUMENT, OTHER
}

enum class DocumentType {
    TITLE, REGISTRATION, INSPECTION_REPORT, SERVICE_RECORDS, WARRANTY, OTHER
}

enum class DamageSeverity {
    MINOR, MODERATE, MAJOR, SEVERE
}

enum class TransmissionType {
    AUTOMATIC, MANUAL, CVT, SEMI_AUTOMATIC, DUAL_CLUTCH
}

enum class DrivetrainType {
    FWD, RWD, AWD, FOUR_WD
}

enum class FuelType {
    GASOLINE, DIESEL, ELECTRIC, HYBRID, PLUGIN_HYBRID, FLEX_FUEL, OTHER
}

enum class VehicleCondition {
    EXCELLENT, GOOD, FAIR, POOR, DAMAGED, SALVAGE
}

enum class AuctionType {
    LIVE, ONLINE, SEALED_BID, BUY_IT_NOW, MAKE_OFFER
}

enum class SellerType {
    DEALER, PRIVATE, AUCTION_HOUSE, LEASING_COMPANY, INSURANCE
}

enum class TitleStatus {
    CLEAN, SALVAGE, REBUILT, FLOOD, LEMON, PARTS_ONLY, NONE
}

enum class FeeType {
    BUYER_PREMIUM, DOCUMENTATION, SHIPPING, TAX, OTHER
}

enum class ShippingMethod {
    PICKUP, DELIVERY, FREIGHT, WHITE_GLOVE
}

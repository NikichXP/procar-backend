package com.procar.provider.admin

import com.procar.provider.common.GeoCoordinates
import com.procar.provider.common.PaginationResponse
import com.procar.provider.lot.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class AdminCreateLotRequest(
    val externalId: String,

    val title: String,

    val description: String,

    @field:Valid
    val vehicle: AdminVehicleInfoRequest,

    @field:Valid
    val auction: AdminAuctionInfoRequest,

    @field:Valid
    val location: AdminLocationInfoRequest,

    @field:Valid
    val metadata: AdminLotMetadataRequest,

    val status: LotStatus,

    val brokerOrgId: String? = null
)

data class AdminUpdateLotRequest(
    val title: String? = null,
    val description: String? = null,
    val vehicle: AdminVehicleInfoRequest? = null,
    val auction: AdminAuctionInfoRequest? = null,
    val location: AdminLocationInfoRequest? = null,
    val metadata: AdminLotMetadataRequest? = null,
    val status: LotStatus? = null,
    val brokerOrgId: String? = null
)

data class AdminUpdateStatusRequest(
    val status: LotStatus
)

data class AdminHiddenRequest(
    val hidden: Boolean
)

data class AdminVehicleInfoRequest(
    val vin: String? = null,
    val make: String,
    val model: String,
    @field:Positive val year: Int,
    val trim: String? = null,
    val bodyType: BodyType,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,

    @field:Valid
    val engine: AdminEngineInfoRequest,

    val transmission: TransmissionType,

    val drivetrain: DrivetrainType,

    val fuelType: FuelType,

    val condition: VehicleCondition,

    val features: List<String> = emptyList(),
    val images: List<AdminVehicleImageRequest> = emptyList(),
    val documents: List<AdminDocumentRequest> = emptyList(),
    val damage: List<AdminDamageInfoRequest>? = null
)

data class AdminEngineInfoRequest(
    val type: String,
    val displacement: Double? = null,
    val cylinders: Int? = null,
    val horsepower: Int? = null,
    val torque: Int? = null
)

data class AdminVehicleImageRequest(
    val url: String,
    val type: ImageType,
    val description: String? = null,
    val isPrimary: Boolean = false
)

data class AdminDocumentRequest(
    val type: DocumentType,
    val url: String,
    val description: String? = null
)

data class AdminDamageInfoRequest(
    val area: String,
    val severity: DamageSeverity,
    val description: String,
    val estimatedRepairCost: Double? = null
)

data class AdminAuctionInfoRequest(
    @field:Positive val currentBid: Double,
    @field:Positive val startingBid: Double,
    val reservePrice: Double? = null,
    @field:Positive val bidIncrement: Double,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timeRemaining: Long? = null,
    val auctionType: AuctionType,
    val buyItNowPrice: Double? = null
)

data class AdminLocationInfoRequest(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates? = null,
    val timezone: String
)

data class AdminLotMetadataRequest(
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),

    @field:Valid
    val sellerInfo: AdminSellerInfoRequest,

    val inspection: AdminInspectionInfoRequest? = null,
    val history: AdminVehicleHistoryRequest? = null,
    val fees: List<AdminFeeRequest> = emptyList(),
    val shipping: AdminShippingInfoRequest? = null
)

data class AdminSellerInfoRequest(
    val id: String,
    val name: String,
    val type: SellerType,
    val rating: Double? = null,
    val totalSales: Int? = null,
    val responseRate: Double? = null
)

data class AdminInspectionInfoRequest(
    val inspected: Boolean = false,
    val inspectionDate: LocalDateTime? = null,
    val inspector: String? = null,
    val reportUrl: String? = null,
    val overallCondition: String? = null,
    val keyFindings: List<String> = emptyList()
)

data class AdminVehicleHistoryRequest(
    val accidents: Int = 0,
    val owners: Int = 1,
    val titleStatus: TitleStatus,
    val serviceRecords: Boolean = false,
    val lastServiceDate: LocalDateTime? = null
)

data class AdminFeeRequest(
    val type: FeeType,
    @field:Positive val amount: Double,
    val description: String,
    val mandatory: Boolean = false
)

data class AdminShippingInfoRequest(
    val available: Boolean = false,
    val estimatedCost: Double? = null,
    val methods: List<ShippingMethod> = emptyList(),
    val restrictions: List<String> = emptyList()
)

data class AdminLotResponse(
    val id: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoResponse,
    val auction: AdminAuctionInfoResponse,
    val location: AdminLocationInfoResponse,
    val metadata: AdminLotMetadataResponse,
    val status: LotStatus,
    val brokerOrgId: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AdminVehicleInfoResponse(
    val vin: String?,
    val make: String,
    val model: String,
    val year: Int,
    val trim: String?,
    val bodyType: String,
    val color: String?,
    val interiorColor: String?,
    val mileage: Int?,
    val engine: AdminEngineInfoResponse,
    val transmission: TransmissionType,
    val drivetrain: DrivetrainType,
    val fuelType: FuelType,
    val condition: VehicleCondition,
    val features: List<String>,
    val images: List<AdminVehicleImageResponse>,
    val documents: List<AdminDocumentResponse>,
    val damage: List<AdminDamageInfoResponse>?
)

data class AdminEngineInfoResponse(
    val type: String,
    val displacement: Double?,
    val cylinders: Int?,
    val horsepower: Int?,
    val torque: Int?
)

data class AdminVehicleImageResponse(
    val url: String,
    val type: ImageType,
    val description: String?,
    val isPrimary: Boolean
)

data class AdminDocumentResponse(
    val type: DocumentType,
    val url: String,
    val description: String?
)

data class AdminDamageInfoResponse(
    val area: String,
    val severity: DamageSeverity,
    val description: String,
    val estimatedRepairCost: Double?
)

data class AdminAuctionInfoResponse(
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

data class AdminLocationInfoResponse(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates?,
    val timezone: String
)

data class AdminLotMetadataResponse(
    val tags: List<String>,
    val categories: List<String>,
    val sellerInfo: AdminSellerInfoResponse,
    val inspection: AdminInspectionInfoResponse?,
    val history: AdminVehicleHistoryResponse?,
    val fees: List<AdminFeeResponse>,
    val shipping: AdminShippingInfoResponse?
)

data class AdminSellerInfoResponse(
    val id: String,
    val name: String,
    val type: SellerType,
    val rating: Double?,
    val totalSales: Int?,
    val responseRate: Double?
)

data class AdminInspectionInfoResponse(
    val inspected: Boolean,
    val inspectionDate: LocalDateTime?,
    val inspector: String?,
    val reportUrl: String?,
    val overallCondition: String?,
    val keyFindings: List<String>
)

data class AdminVehicleHistoryResponse(
    val accidents: Int,
    val owners: Int,
    val titleStatus: TitleStatus,
    val serviceRecords: Boolean,
    val lastServiceDate: LocalDateTime?
)

data class AdminFeeResponse(
    val type: FeeType,
    val amount: Double,
    val description: String,
    val mandatory: Boolean
)

data class AdminShippingInfoResponse(
    val available: Boolean,
    val estimatedCost: Double?,
    val methods: List<ShippingMethod>,
    val restrictions: List<String>
)

data class AdminPaginatedLotsResponse(
    val lots: List<AdminLotResponse>,
    val pagination: PaginationResponse
)

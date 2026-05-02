package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
enum class AdminLotStatus {
    DRAFT,
    PENDING,
    ACTIVE,
    CLOSED,
    CANCELLED,
}

@Serializable
data class AdminLotResponse(
    val id: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoResponse,
    val auction: AdminAuctionInfoResponse? = null,
    val location: AdminLocationInfoResponse,
    val status: AdminLotStatus,
    val createdAt: String,
    val updatedAt: String,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminVehicleInfoResponse(
    val make: String,
    val model: String,
    val year: Int,
    val bodyType: String,
    val transmission: String,
    val fuelType: String,
    val condition: String,
    val vin: String? = null,
    val trim: String? = null,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,
    val drivetrain: String? = null,
    val engine: AdminEngineInfoResponse? = null,
    val images: List<AdminVehicleImageResponse> = emptyList(),
)

@Serializable
data class AdminVehicleImageResponse(
    val url: String,
    val type: String,
    val description: String? = null,
    val isPrimary: Boolean = false,
)

@Serializable
data class AdminEngineInfoResponse(
    val type: String,
    val displacement: Double? = null,
    val cylinders: Int? = null,
    val horsepower: Int? = null,
    val torque: Int? = null,
)

@Serializable
data class AdminAuctionInfoResponse(
    val currentBid: Double,
    val startingBid: Double,
    val bidIncrement: Double,
    val startTime: String,
    val endTime: String,
    val totalBids: Int = 0,
    val reservePrice: Double? = null,
)

@Serializable
data class AdminLocationInfoResponse(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
)

@Serializable
data class AdminCreateLotRequest(
    val brokerId: String,
    val warehouseId: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoRequest,
    val auction: AdminAuctionInfoRequest? = null,
    val metadata: AdminLotMetadataRequest = AdminLotMetadataRequest(),
    val status: AdminLotStatus,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminVehicleInfoRequest(
    val make: String,
    val model: String,
    val year: Int,
    val bodyType: String,
    val transmission: String,
    val drivetrain: String,
    val fuelType: String,
    val condition: String,
    val engine: AdminEngineInfoRequest,
    val vin: String? = null,
    val trim: String? = null,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,
)

@Serializable
data class AdminEngineInfoRequest(val type: String)

@Serializable
data class AdminAuctionInfoRequest(
    val currentBid: Double,
    val startingBid: Double,
    val bidIncrement: Double,
    val startTime: String,
    val endTime: String,
    val reservePrice: Double? = null,
)

@Serializable
data class AdminLocationInfoRequest(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
)

@Serializable
data class AdminLotMetadataRequest(
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
)

@Serializable
data class AdminUpdateLotRequest(
    val title: String? = null,
    val description: String? = null,
    val vehicle: AdminVehicleInfoRequest? = null,
    val auction: AdminAuctionInfoRequest? = null,
    val location: AdminLocationInfoRequest? = null,
    val metadata: AdminLotMetadataRequest? = null,
    val status: AdminLotStatus? = null,
    val lotType: LotType? = null,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminPaginatedLotsResponse(
    val lots: List<AdminLotResponse>,
    val pagination: PaginationResponse = PaginationResponse(hasNext = false),
)
